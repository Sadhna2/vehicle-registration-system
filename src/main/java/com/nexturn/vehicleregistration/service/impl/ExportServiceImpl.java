package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.OwnerPaymentDetail;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;
import com.nexturn.vehicleregistration.entity.Vehicle;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.PaymentStatus;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.repository.RegistrationFeeRuleRepository;
import com.nexturn.vehicleregistration.repository.VechileRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.ExportService;
import com.nexturn.vehicleregistration.util.CsvSupport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private OwnerPaymentRepository paymentRepository;

    @Autowired
    private VechileRepository vehicleRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Autowired
    private RegistrationFeeRuleRepository feeRuleRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public CsvFile applications(Long ownerId, ApplicationStatus status) {
        Sort sort = Sort.by("submittedDate").descending();
        List<VehicleRegistrationApplication> applications = ownerId == null
                ? applicationRepository.findAll(sort)
                : applicationRepository.findByApplicantOwnerId(ownerId, sort);
        Map<String, OwnerPaymentDetail> payments = paymentRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getApplication().getApplicationRefNo(), Function.identity()));
        List<List<Object>> rows = new ArrayList<>();
        for (VehicleRegistrationApplication a : applications) {
            if (status != null && a.getApplicationStatus() != status) {
                continue;
            }
            Vehicle v = a.getVehicle();
            Owner o = a.getApplicant();
            OwnerPaymentDetail payment = payments.get(a.getApplicationRefNo());
            rows.add(Arrays.asList(
                    a.getApplicationRefNo(), a.getApplicationType(), a.getApplicationStatus(),
                    o.getFirstName() + " " + o.getLastName(), o.getEmailAddress(),
                    v.getManufacturerName() + " " + v.getModelName(), v.getVehicleCategory(),
                    v.getChassisNumber(), a.getPayableAmount(),
                    payment != null && payment.getPaymentStatus() == PaymentStatus.SUCCESS ? "YES" : "NO",
                    a.getInspectionStatus(), v.getRegistrationcertificateNumber(),
                    a.getSubmittedDate(), a.getUpdatedDate()));
        }
        return file("applications", rows, List.of(
                "reference", "type", "status", "owner", "ownerEmail", "vehicle", "vehicleCategory",
                "chassisNumber", "fee", "paid", "inspection", "registrationNumber", "submittedAt", "updatedAt"));
    }

    @Override
    public CsvFile vehicles(Long ownerId) {
        List<Vehicle> vehicles = ownerId == null
                ? vehicleRepository.findAll(Sort.by("temporaryregisterNo"))
                : vehicleRepository.findByCurrentOwnerOwnerId(ownerId);
        List<List<Object>> rows = new ArrayList<>();
        for (Vehicle v : vehicles) {
            Owner o = v.getCurrentOwner();
            rows.add(Arrays.asList(
                    v.getTemporaryregisterNo(), o.getFirstName() + " " + o.getLastName(),
                    v.getVehicleCategory(), v.getManufacturerName(), v.getModelName(),
                    v.getChassisNumber(), v.getEngineNumber(), v.getFuelType(), v.getManufactureYear(),
                    v.getColorVariant(), v.getRegistrationcertificateNumber(),
                    v.getFirstRegistrationDate(), v.getRegistrationValidTill()));
        }
        return file("vehicles", rows, List.of(
                "vehicleId", "owner", "vehicleCategory", "manufacturer", "model", "chassisNumber",
                "engineNumber", "fuelType", "manufactureYear", "colour", "registrationNumber",
                "firstRegistrationDate", "validTill"));
    }

    @Override
    public CsvFile owners() {
        List<List<Object>> rows = new ArrayList<>();
        for (Owner o : ownerRepository.findAll(Sort.by("ownerId"))) {
            rows.add(Arrays.asList(
                    o.getOwnerId(), o.getFirstName(), o.getLastName(), o.getEmailAddress(),
                    o.getPhoneNumber(), o.getIdentityProofType(), mask(o.getIdentityProofNumber()),
                    o.getCityName(), o.getStateName(), o.getPincode(), o.getStatus(), o.getDateOfCreation()));
        }
        return file("owners", rows, List.of(
                "ownerId", "firstName", "lastName", "emailAddress", "phoneNumber", "identityProofType",
                "identityProofNumber", "city", "state", "pincode", "status", "createdAt"));
    }

    @Override
    public CsvFile employees() {
        List<List<Object>> rows = new ArrayList<>();
        for (RTOEmployee e : employeeRepository.findAll(Sort.by("employeeId"))) {
            rows.add(Arrays.asList(
                    e.getEmployeeId(), e.getFirstName(), e.getLastName(), e.getEmailAddress(),
                    e.getPhoneNumber(), e.getDesignation(), e.getRole(), e.getStatus(), e.getDateOfJoining()));
        }
        return file("employees", rows, List.of(
                "employeeId", "firstName", "lastName", "emailAddress", "phoneNumber", "designation",
                "role", "status", "joinedAt"));
    }

    /** Same column names the fee import accepts, so an export can be edited and imported back. */
    @Override
    public CsvFile feeRules() {
        List<List<Object>> rows = new ArrayList<>();
        for (RegistrationFeeRule f : feeRuleRepository.findAll(Sort.by("effectiveFrom").descending())) {
            rows.add(Arrays.asList(
                    f.getFeeRuleId(), f.getVehicleCategory(), f.getApplicationType(),
                    f.getFeeAmount(), f.getEffectiveFrom()));
        }
        return file("fee-rules", rows, List.of(
                "feeRuleId", "vehicleCategory", "applicationType", "feeAmount", "effectiveFrom"));
    }

    private CsvFile file(String dataset, List<List<Object>> rows, List<String> header) {
        auditService.record(null, "EXPORT", dataset + ": " + rows.size() + " rows");
        return new CsvFile(dataset + "-" + LocalDate.now() + ".csv", CsvSupport.write(header, rows));
    }

    /** Identity numbers are personal data: exports show only the last four characters. */
    private static String mask(String value) {
        if (value == null || value.length() <= 4) {
            return value;
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
