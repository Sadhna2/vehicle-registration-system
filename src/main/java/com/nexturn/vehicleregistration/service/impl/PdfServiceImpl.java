package com.nexturn.vehicleregistration.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerPaymentDetailResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.PaymentStatus;
import com.nexturn.vehicleregistration.exception.RecordNotFoundException;
import com.nexturn.vehicleregistration.exception.RegistrationCertificateNotFoundException;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.PdfService;
import com.nexturn.vehicleregistration.service.ReportService;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdfServiceImpl implements PdfService {

    private static final Color BRAND = new Color(22, 104, 84);
    private static final Color MUTED = new Color(101, 124, 112);
    private static final Font TITLE = new Font(Font.HELVETICA, 18, Font.BOLD, BRAND);
    private static final Font HEADING = new Font(Font.HELVETICA, 11, Font.BOLD, BRAND);
    private static final Font LABEL = new Font(Font.HELVETICA, 9, Font.NORMAL, MUTED);
    private static final Font VALUE = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font VALUE_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font SMALL = new Font(Font.HELVETICA, 8, Font.NORMAL, MUTED);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuditService auditService;

    @Override
    public PdfFile certificate(String referenceNumber, Long ownerId) {
        ApplicationDetailsResponse application = applicationQueryService.get(referenceNumber, ownerId);
        RegistrationCertificateResponse rc = application.certificate();
        if (application.applicationStatus() != ApplicationStatus.APPROVED || rc == null) {
            throw new RegistrationCertificateNotFoundException(referenceNumber);
        }
        VehicleResponse vehicle = application.vehicle();
        OwnerResponse owner = vehicle.currentOwner() != null ? vehicle.currentOwner() : application.applicant();
        byte[] pdf = render(PageSize.A4, document -> {
            header(document, "Certificate of Registration", "Registration number " + rc.registrationNumber());
            section(document, "Owner and registration", new String[][] {
                {"Registration number", rc.registrationNumber()},
                {"Registered owner", rc.registeredOwnerName()},
                {"Address", join(owner.address(), owner.cityName(), owner.stateName(), owner.pincode())},
                {"Date of registration", date(vehicle.firstRegistrationDate())},
                {"Registration valid up to", date(rc.validTill())},
                {"Date of issue", date(rc.issuedDate())},
                {"Issued by", rc.issuedByEmployeeName()},
                {"Application reference", rc.applicationRefNo()},
            });
            section(document, "Vehicle particulars", new String[][] {
                {"Class of vehicle", label(vehicle.vehicleCategory())},
                {"Manufacturer", vehicle.manufacturerName()},
                {"Model", vehicle.modelName()},
                {"Year of manufacture", String.valueOf(vehicle.manufactureYear())},
                {"Chassis number", vehicle.chassisNumber()},
                {"Engine number", vehicle.engineNumber()},
                {"Fuel used", label(vehicle.fuelType())},
                {"Colour", vehicle.colorVariant()},
            });
            footer(document, "This is a computer-generated certificate issued through the Vehicle Registration System.");
        });
        return new PdfFile("RC-" + rc.registrationNumber() + ".pdf", pdf);
    }

    @Override
    public PdfFile receipt(String referenceNumber, Long ownerId) {
        ApplicationDetailsResponse application = applicationQueryService.get(referenceNumber, ownerId);
        OwnerPaymentDetailResponse payment = application.payment();
        if (payment == null || payment.paymentStatus() != PaymentStatus.SUCCESS) {
            throw new RecordNotFoundException("No successful payment is recorded for " + referenceNumber);
        }
        OwnerResponse owner = application.applicant();
        VehicleResponse vehicle = application.vehicle();
        byte[] pdf = render(PageSize.A5, document -> {
            header(document, "Payment receipt", "Registration fee");
            section(document, "Receipt", new String[][] {
                {"Receipt number", payment.transactionReferenceId()},
                {"Paid on", date(payment.paidAt())},
                {"Amount paid", money(payment.amountPaid())},
                {"Payment method", label(payment.paymentMethod()) + " ending " + payment.cardLastFourDigit()},
                {"Status", label(payment.paymentStatus())},
            });
            section(document, "Application", new String[][] {
                {"Application reference", application.applicationRefNo()},
                {"Paid by", owner.firstName() + " " + owner.lastName()},
                {"Vehicle", vehicle.manufacturerName() + " " + vehicle.modelName()},
                {"Vehicle class", label(vehicle.vehicleCategory())},
            });
            footer(document, "Simulated payment recorded by the Vehicle Registration System. No card data is stored.");
        });
        return new PdfFile("receipt-" + application.applicationRefNo() + ".pdf", pdf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PdfFile applicationsReport() {
        Map<String, Object> report = reportService.report();
        Map<String, Long> counts = (Map<String, Long>) report.get("counts");
        List<ApplicationSummaryResponse> applications = applicationQueryService.list(null);
        byte[] pdf = render(PageSize.A4.rotate(), document -> {
            header(document, "Applications report", "Generated " + date(LocalDate.now()));
            section(document, "Summary", new String[][] {
                {"Fees collected", money((BigDecimal) report.get("feeCollected"))},
                {"Vehicle owners", String.valueOf(report.get("owners"))},
                {"Vehicle records", String.valueOf(report.get("vehicles"))},
                {"Registrations expiring within 90 days", String.valueOf(report.get("expiringWithin90Days"))},
            });
            section(document, "Applications by status", counts.entrySet().stream()
                    .map(entry -> new String[] {label(entry.getKey()), String.valueOf(entry.getValue())})
                    .toArray(String[][]::new));
            document.add(new Paragraph("Application register (" + applications.size() + ")", HEADING));
            PdfPTable table = new PdfPTable(new float[] {3.2f, 2.2f, 2.4f, 1.8f, 1.4f, 1.6f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            table.setHeaderRows(1);
            Stream.of("Reference", "Owner", "Vehicle", "Status", "Fee", "Submitted")
                    .forEach(title -> table.addCell(headerCell(title)));
            for (ApplicationSummaryResponse a : applications) {
                Stream.of(a.reference(), a.owner(), a.vehicle(), label(a.status()),
                                money(a.amount()), date(a.submittedAt()))
                        .forEach(text -> table.addCell(bodyCell(text)));
            }
            document.add(table);
            footer(document, "Vehicle Registration System - department report.");
        });
        auditService.record(null, "EXPORT", "applications report PDF: " + applications.size() + " rows");
        return new PdfFile("applications-report-" + LocalDate.now() + ".pdf", pdf);
    }

    // ------------------------------------------------------------------ layout helpers

    private static byte[] render(Rectangle size, Consumer<Document> content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(size, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.addTitle("Vehicle Registration System");
        document.addCreator("Vehicle Registration System");
        document.open();
        content.accept(document);
        document.close();
        return out.toByteArray();
    }

    private static void header(Document document, String title, String subtitle) {
        document.add(new Paragraph("VEHICLE REGISTRATION SYSTEM", SMALL));
        Paragraph heading = new Paragraph(title, TITLE);
        heading.setSpacingBefore(4);
        document.add(heading);
        document.add(new Paragraph(subtitle, LABEL));
        document.add(new Chunk("\n"));
    }

    private static void section(Document document, String title, String[][] rows) {
        document.add(new Paragraph(title, HEADING));
        PdfPTable table = new PdfPTable(new float[] {1.4f, 2.6f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);
        for (String[] row : rows) {
            PdfPCell name = new PdfPCell(new Phrase(row[0], LABEL));
            PdfPCell value = new PdfPCell(new Phrase(Objects.requireNonNullElse(row[1], "-"), VALUE_BOLD));
            for (PdfPCell cell : new PdfPCell[] {name, value}) {
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(new Color(224, 230, 218));
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
        document.add(table);
    }

    private static PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(BRAND);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell bodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(Objects.requireNonNullElse(text, "-"), VALUE));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderColor(new Color(224, 230, 218));
        return cell;
    }

    private static void footer(Document document, String text) {
        Paragraph footer = new Paragraph(text, SMALL);
        footer.setSpacingBefore(16);
        document.add(footer);
    }

    private static String label(Object value) {
        return value == null ? "-" : value.toString().replace('_', ' ');
    }

    private static String join(String... parts) {
        return Stream.of(parts).filter(p -> p != null && !p.isBlank()).collect(Collectors.joining(", "));
    }

    /** Standard PDF fonts have no rupee sign, so amounts are written as "INR 1,500.00". */
    private static String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.of("en", "IN"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "INR " + format.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static String date(LocalDate value) {
        return value == null ? "-" : DATE.format(value);
    }

    private static String date(Instant value) {
        return value == null ? "-" : DATE.format(value.atZone(ZoneId.systemDefault()));
    }
}
