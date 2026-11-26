package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.ImportResultResponse;
import com.nexturn.vehicleregistration.dto.response.ImportResultResponse.RowError;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.enums.VehicleCategory;
import com.nexturn.vehicleregistration.exception.InvalidRequestException;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.ImportService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;
import com.nexturn.vehicleregistration.util.CsvSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportServiceImpl implements ImportService {

    private static final long MAX_FILE_BYTES = 1024 * 1024;
    private static final int MAX_ROWS = 500;
    private static final List<String> FEE_COLUMNS =
            List.of("vehicleCategory", "applicationType", "feeAmount", "effectiveFrom");
    private static final List<String> EMPLOYEE_COLUMNS = List.of(
            "firstName", "lastName", "emailAddress", "phoneNumber", "password", "designation", "role");

    @Autowired
    private Validator validator;

    @Autowired
    private RegistrationFeeRuleService feeRuleService;

    @Autowired
    private RTOEmployeeService employeeService;

    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public ImportResultResponse importFeeRules(MultipartFile file, boolean dryRun) {
        return importRows(file, dryRun, FEE_COLUMNS, "fee rules", (row) -> {
            RegistrationFeeRuleRequest request = new RegistrationFeeRuleRequest(
                    row.enumValue("vehicleCategory", VehicleCategory.class),
                    row.enumValue("applicationType", ApplicationType.class),
                    row.parse("feeAmount", BigDecimal::new),
                    row.parse("effectiveFrom", LocalDate::parse));
            if (request.applicationType() != null && request.applicationType() != ApplicationType.NEW) {
                row.error("applicationType", "Only NEW registration fee rules are supported");
            }
            row.validate(request);
            return () -> feeRuleService.fee(request);
        });
    }

    @Override
    public ImportResultResponse importEmployees(
            MultipartFile file, boolean dryRun, SessionRole creatorRole) {
        Set<String> emailsInFile = new HashSet<>();
        return importRows(file, dryRun, EMPLOYEE_COLUMNS, "employees", (row) -> {
            RTOEmployeeRequest request = new RTOEmployeeRequest(
                    row.text("firstName"), row.text("lastName"), row.text("emailAddress"),
                    row.text("phoneNumber"), row.text("password"),
                    row.enumValue("designation", RTOEmployeeDesignation.class),
                    row.enumValue("role", RTOEmployeeRole.class));
            row.validate(request);
            if (creatorRole != SessionRole.SYSTEM_ADMIN
                    && request.role() != null && request.role() != RTOEmployeeRole.RTO_OFFICER) {
                row.error("role", "RTO administrators can only create RTO officer accounts");
            }
            if (request.emailAddress() != null && !request.emailAddress().isBlank()) {
                String email = request.emailAddress().trim().toLowerCase(Locale.ROOT);
                if (!emailsInFile.add(email)) {
                    row.error("emailAddress", "This email appears more than once in the file");
                } else if (employeeRepository.findByEmailAddress(email).isPresent()) {
                    row.error("emailAddress", "An employee with this email already exists");
                }
            }
            return () -> employeeService.employee(request, creatorRole);
        });
    }

    /** One parsed CSV line: reads columns by name and collects that line's errors. */
    private final class Row {
        private final int line;
        private final Map<String, String> values;
        private final List<RowError> errors;

        Row(int line, Map<String, String> values, List<RowError> errors) {
            this.line = line;
            this.values = values;
            this.errors = errors;
        }

        String text(String column) {
            String value = values.getOrDefault(column, "").trim();
            return value.isEmpty() ? null : value;
        }

        /** Parses a number or date column; records an error and returns null if it is invalid. */
        <T> T parse(String column, Function<String, T> parser) {
            String value = text(column);
            if (value == null) {
                return null;
            }
            try {
                return parser.apply(value);
            } catch (IllegalArgumentException | DateTimeParseException invalid) {
                error(column, "Invalid value \"" + value + "\"");
                return null;
            }
        }

        /** Parses an enum column in any letter case, e.g. "private_car" or "PRIVATE_CAR". */
        <E extends Enum<E>> E enumValue(String column, Class<E> type) {
            String value = text(column);
            if (value == null) {
                return null;
            }
            try {
                return Enum.valueOf(type, value.toUpperCase(Locale.ROOT).replace(' ', '_'));
            } catch (IllegalArgumentException invalid) {
                error(column, "Invalid value \"" + value + "\"");
                return null;
            }
        }

        void validate(Object request) {
            for (ConstraintViolation<Object> violation : validator.validate(request)) {
                String field = violation.getPropertyPath().toString();
                boolean alreadyReported = errors.stream()
                        .anyMatch(e -> e.row() == line && e.field().equals(field));
                if (!alreadyReported) {
                    error(field, violation.getMessage());
                }
            }
        }

        void error(String field, String message) {
            errors.add(new RowError(line, field, message));
        }
    }

    private ImportResultResponse importRows(
            MultipartFile file, boolean dryRun, List<String> required, String dataset,
            Function<Row, Runnable> rowReader) {
        List<List<String>> lines = CsvSupport.parse(readText(file));
        List<RowError> errors = new ArrayList<>();
        if (lines.isEmpty()) {
            throw new InvalidRequestException("The CSV file is empty");
        }
        // Map each required column name (any letter case) to its position; extra columns are ignored.
        Map<String, Integer> columns = new HashMap<>();
        List<String> header = lines.get(0);
        for (int i = 0; i < header.size(); i++) {
            String name = header.get(i).trim();
            for (String column : required) {
                if (column.equalsIgnoreCase(name)) {
                    columns.putIfAbsent(column, i);
                }
            }
        }
        List<String> missing = required.stream().filter(c -> !columns.containsKey(c)).toList();
        if (!missing.isEmpty()) {
            throw new InvalidRequestException(
                    "The header row is missing: " + String.join(", ", missing)
                            + ". Expected columns: " + String.join(", ", required));
        }
        int dataRows = lines.size() - 1;
        if (dataRows == 0) {
            throw new InvalidRequestException("The CSV file has a header but no data rows");
        }
        if (dataRows > MAX_ROWS) {
            throw new InvalidRequestException("Import at most " + MAX_ROWS + " rows at a time");
        }

        List<Runnable> saves = new ArrayList<>();
        int validRows = 0;
        for (int index = 1; index < lines.size(); index++) {
            List<String> cells = lines.get(index);
            Map<String, String> values = new HashMap<>();
            columns.forEach((name, position) ->
                    values.put(name, position < cells.size() ? cells.get(position) : ""));
            int before = errors.size();
            Row row = new Row(index + 1, values, errors);
            Runnable save = rowReader.apply(row);
            if (errors.size() == before) {
                validRows++;
                saves.add(save);
            }
        }

        int imported = 0;
        if (!dryRun && errors.isEmpty()) {
            saves.forEach(Runnable::run);
            imported = saves.size();
            auditService.record(null, "IMPORT", dataset + ": " + imported + " rows");
        }
        return new ImportResultResponse(dryRun, dataRows, validRows, imported, errors);
    }

    private static String readText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Choose a CSV file to import");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new InvalidRequestException("The CSV file must be smaller than 1 MB");
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(file.getBytes()))
                    .toString();
        } catch (CharacterCodingException notUtf8) {
            throw new InvalidRequestException("Save the CSV file as UTF-8 and try again");
        } catch (IOException failure) {
            throw new InvalidRequestException("The file could not be read. Please try again.");
        }
    }
}
