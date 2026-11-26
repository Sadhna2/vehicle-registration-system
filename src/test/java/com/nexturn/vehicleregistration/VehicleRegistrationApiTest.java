package com.nexturn.vehicleregistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.util.PasswordUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VehicleRegistrationApiTest {
    @LocalServerPort private int port;
    @Autowired private OwnerRepository owners;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String PASSWORD = "TestPassword123!";
    private static final String ADMIN_EMAIL = "sysadmin@example.test";
    private static final String ADMIN_PASSWORD = "AdminPassword123!";
    private static final byte[] PDF = "%PDF-1.4\n% vehicle document\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    private static final String[] DOCUMENT_TYPES = {"INVOICE", "INSURANCE", "IDENTITY_PROOF"};

    private String email;
    private long ownerId;
    private String ownerToken;
    private long officerId;
    private String officerToken;
    private String rtoAdminToken;
    private String systemAdminToken;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> "jdbc:h2:mem:registration-tests;MODE=MySQL;DB_CLOSE_DELAY=-1");
        properties.add("spring.datasource.username", () -> "sa");
        properties.add("spring.datasource.password", () -> "");
        properties.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        properties.add("spring.jpa.show-sql", () -> "false");
        properties.add("spring.config.import", () -> "");
        properties.add("vrs.jwt.secret", () -> "test-only-secret-" + UUID.randomUUID() + UUID.randomUUID());
        properties.add("vrs.bootstrap.admin-email", () -> ADMIN_EMAIL);
        properties.add("vrs.bootstrap.admin-password", () -> ADMIN_PASSWORD);
        properties.add("vrs.documents.storage-location", () -> {
            try {
                return Files.createTempDirectory("vrs-test-uploads").toString();
            } catch (java.io.IOException failure) {
                throw new IllegalStateException(failure);
            }
        });
    }

    // ---------------------------------------------------------------- HTTP helpers

    private HttpResponse<String> raw(String method, String path, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        headers.forEach(request::header);
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode api(String method, String path, Object body, int status, String token) throws Exception {
        Map<String, String> headers = token == null ? Map.of() : Map.of("Authorization", "Bearer " + token);
        HttpResponse<String> response = raw(method, path, body == null ? null : json.writeValueAsString(body), headers);
        assertEquals(status, response.statusCode(), method + " " + path + ": " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    private String login(String accountEmail, String password, boolean staff) throws Exception {
        return api("POST", "/api/auth/login", Map.of("email", accountEmail, "password", password, "staff", staff), 200, null)
                .path("accessToken").asString();
    }

    private Map<String, Object> owner(String accountEmail) {
        return Map.ofEntries(Map.entry("firstName", "Test"), Map.entry("lastName", "Owner"),
                Map.entry("emailAddress", accountEmail), Map.entry("phoneNumber", "9876543210"),
                Map.entry("password", PASSWORD), Map.entry("dateOfBirth", "1995-01-01"),
                Map.entry("identityProofType", "PAN"), Map.entry("identityProofNumber", UUID.randomUUID().toString().substring(0, 20)),
                Map.entry("address", "12 Test Road"), Map.entry("cityName", "Pune"),
                Map.entry("stateName", "Maharashtra"), Map.entry("pincode", "411001"));
    }

    private Map<String, Object> employee(String role) {
        return Map.of("firstName", "Test", "lastName", "Staff",
                "emailAddress", "staff-" + UUID.randomUUID() + "@example.com", "phoneNumber", "9876543211",
                "password", PASSWORD, "designation", "OFFICER", "role", role);
    }

    private Map<String, Object> vehicle() {
        return Map.of("vehicleCategory", "PRIVATE_CAR", "manufacturerName", "Maruti Suzuki", "modelName", "Swift",
                "chassisNumber", "CH" + UUID.randomUUID(), "engineNumber", "EN" + UUID.randomUUID(),
                "fuelType", "PETROL", "manufactureYear", Year.now().getValue(), "colorVariant", "White");
    }

    /** Creates a staff account as the system admin and signs in as it. */
    private JsonNode createStaff(String role) throws Exception {
        return api("POST", "/api/employees", employee(role), 200, systemAdminToken);
    }

    private String staffToken(JsonNode staff) throws Exception {
        return login(staff.path("emailAddress").asString(), PASSWORD, true);
    }

    @BeforeEach
    void createSignedInUsers() throws Exception {
        systemAdminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD, true);

        email = "owner-" + UUID.randomUUID() + "@example.com";
        JsonNode signup = api("POST", "/api/auth/signup", owner(email), 200, null);
        ownerId = signup.path("user").path("id").asLong();
        ownerToken = signup.path("accessToken").asString();

        JsonNode officer = createStaff("RTO_OFFICER");
        officerId = officer.path("employeeId").asLong();
        officerToken = staffToken(officer);
        rtoAdminToken = staffToken(createStaff("RTO_ADMIN"));

        api("POST", "/api/fees", Map.of("vehicleCategory", "PRIVATE_CAR", "applicationType", "NEW",
                "feeAmount", 600, "effectiveFrom", LocalDate.now().toString()), 200, rtoAdminToken);
    }

    /** Submits an application and uploads all three required documents. */
    private String submit() throws Exception {
        String reference = submitWithoutDocuments();
        for (String type : DOCUMENT_TYPES) {
            upload(reference, type, "document.pdf", PDF, 200, ownerToken);
        }
        return reference;
    }

    private String submitWithoutDocuments() throws Exception {
        return api("POST", "/api/applications", vehicle(), 200, ownerToken).path("applicationRefNo").asString();
    }

    private HttpResponse<String> uploadRaw(String reference, String type, String fileName, byte[] content, String token) throws Exception {
        String boundary = "----vrs" + UUID.randomUUID();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"type\"\r\n\r\n" + type + "\r\n"
                + "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(content);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/applications/" + reference + "/documents"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode upload(String reference, String type, String fileName, byte[] content, int status, String token) throws Exception {
        HttpResponse<String> response = uploadRaw(reference, type, fileName, content, token);
        assertEquals(status, response.statusCode(), "upload " + type + ": " + response.body());
        return json.readTree(response.body());
    }

    private JsonNode documents(String reference, String token) throws Exception {
        return api("GET", "/api/applications/" + reference + "/documents", null, 200, token);
    }

    private long documentId(String reference, String type) throws Exception {
        for (JsonNode document : documents(reference, officerToken)) {
            if (type.equals(document.path("documentType").asString())) return document.path("documentId").asLong();
        }
        throw new AssertionError("No " + type + " document");
    }

    private JsonNode reviewDocument(String reference, String type, String action, String remarks, int status) throws Exception {
        Map<String, Object> body = remarks == null ? Map.of("action", action) : Map.of("action", action, "remarks", remarks);
        return api("POST", "/api/applications/" + reference + "/documents/" + documentId(reference, type) + "/review",
                body, status, officerToken);
    }

    private void verifyAllDocuments(String reference) throws Exception {
        for (String type : DOCUMENT_TYPES) {
            reviewDocument(reference, type, "VERIFY", null, 200);
        }
    }

    private JsonNode review(String reference, String action, int status) throws Exception {
        return api("POST", "/api/applications/" + reference + "/review",
                Map.of("action", action, "remarks", "Test review", "appointment", LocalDate.now().toString()), status, officerToken);
    }

    private void inspected(String reference) throws Exception {
        review(reference, "START", 200);
        verifyAllDocuments(reference);
        review(reference, "VERIFY", 200);
        review(reference, "SCHEDULE", 200);
        review(reference, "PASS", 200);
    }

    private void pay(String reference) throws Exception {
        api("POST", "/api/applications/" + reference + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 200, ownerToken);
    }

    // ---------------------------------------------------------------- authentication

    @Test void ownerLoginIssuesSignedTokenAndNeverReturnsPassword() throws Exception {
        JsonNode result = api("POST", "/api/auth/login", Map.of("email", email, "password", PASSWORD, "staff", false), 200, null);
        assertEquals("Bearer", result.path("tokenType").asString());
        assertEquals(3, result.path("accessToken").asString().split("\\.").length);
        assertEquals(ownerId, result.path("user").path("id").asLong());
        assertEquals("OWNER", result.path("user").path("role").asString());
        assertFalse(result.toString().contains(PASSWORD));
        assertNotEquals(PASSWORD, owners.findById(ownerId).orElseThrow().getPassword());
        assertTrue(PasswordUtil.matches(PASSWORD, owners.findById(ownerId).orElseThrow().getPassword()));
        assertEquals(ownerId, api("GET", "/api/auth/me", null, 200, result.path("accessToken").asString()).path("id").asLong());
    }

    @Test void employeeLoginCarriesEmployeeRole() throws Exception {
        JsonNode me = api("GET", "/api/auth/me", null, 200, officerToken);
        assertEquals(officerId, me.path("id").asLong());
        assertEquals("RTO_OFFICER", me.path("role").asString());
    }

    @Test void incorrectCredentialsAreRejected() throws Exception {
        assertEquals("INVALID_LOGIN", api("POST", "/api/auth/login",
                Map.of("email", email, "password", "WrongPassword123!", "staff", false), 401, null).path("code").asString());
    }

    @Test void requestsWithoutValidTokenAreRejected() throws Exception {
        assertEquals("INVALID_TOKEN", api("GET", "/api/applications", null, 401, null).path("code").asString());
        assertEquals("INVALID_TOKEN", api("GET", "/api/applications", null, 401, "not.a.token").path("code").asString());
        String[] parts = ownerToken.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x." + parts[2];
        api("GET", "/api/applications", null, 401, tampered);
        api("GET", "/api/fees", null, 401, null);
    }

    @Test void logoutRevokesTheToken() throws Exception {
        String token = login(email, PASSWORD, false);
        api("GET", "/api/auth/me", null, 200, token);
        api("POST", "/api/auth/logout", null, 204, token);
        api("GET", "/api/auth/me", null, 401, token);
        api("GET", "/api/auth/me", null, 200, ownerToken);
    }

    @Test void deactivatedAccountsLoseAccessImmediately() throws Exception {
        api("PATCH", "/api/accounts/owners/" + ownerId, Map.of("status", "SUSPENDED"), 200, systemAdminToken);
        api("GET", "/api/applications", null, 401, ownerToken);
        api("POST", "/api/auth/login", Map.of("email", email, "password", PASSWORD, "staff", false), 401, null);
    }

    @Test void invalidOwnerFieldsReturnValidationErrors() throws Exception {
        assertEquals("VALIDATION_FAILED", api("POST", "/api/auth/signup", Map.of(), 400, null).path("code").asString());
        api("POST", "/api/auth/login", Map.of(), 400, null);
    }

    @Test void duplicateOwnerIsRejected() throws Exception {
        api("POST", "/api/auth/signup", owner(email), 409, null);
    }

    // ---------------------------------------------------------------- role and ownership rules

    @Test void rolesAreEnforced() throws Exception {
        String ref = submit();
        assertEquals("ACCESS_DENIED", api("POST", "/api/applications/" + ref + "/review",
                Map.of("action", "START"), 403, ownerToken).path("code").asString());
        api("POST", "/api/applications", vehicle(), 403, officerToken);
        api("GET", "/api/employees", null, 403, ownerToken);
        api("GET", "/api/employees", null, 403, officerToken);
        api("GET", "/api/reports", null, 403, officerToken);
        api("GET", "/api/owners", null, 403, rtoAdminToken);
        api("POST", "/api/fees", Map.of("vehicleCategory", "PRIVATE_CAR", "applicationType", "NEW",
                "feeAmount", 700, "effectiveFrom", LocalDate.now().toString()), 403, officerToken);
        api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 403, officerToken);
    }

    @Test void ownersCannotSeeOrChangeAnotherOwnersApplication() throws Exception {
        String ref = submit();
        String otherToken = api("POST", "/api/auth/signup", owner("other-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("accessToken").asString();
        api("GET", "/api/applications/" + ref, null, 403, otherToken);
        api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 403, otherToken);
        api("GET", "/api/applications/" + ref + "/certificate", null, 403, otherToken);
        assertEquals(0, api("GET", "/api/applications", null, 200, otherToken).size());
        assertEquals(0, api("GET", "/api/vehicles", null, 200, otherToken).size());
        api("GET", "/api/applications/" + ref, null, 200, officerToken);
    }

    @Test void rtoAdminCanOnlyCreateOfficers() throws Exception {
        api("POST", "/api/employees", employee("RTO_OFFICER"), 200, rtoAdminToken);
        api("POST", "/api/employees", employee("SYSTEM_ADMIN"), 403, rtoAdminToken);
        api("POST", "/api/employees", employee("RTO_ADMIN"), 403, rtoAdminToken);
    }

    @Test void systemAdminCannotChangeOwnAccount() throws Exception {
        long adminId = api("GET", "/api/auth/me", null, 200, systemAdminToken).path("id").asLong();
        api("PATCH", "/api/accounts/employees/" + adminId, Map.of("status", "INACTIVE"), 409, systemAdminToken);
    }

    // ---------------------------------------------------------------- registration workflow

    @Test void submissionAndQueriesAreScopedToTheOwner() throws Exception {
        String reference = submit();
        assertTrue(reference.startsWith("VRS-"));
        assertEquals("SUBMITTED", api("GET", "/api/applications/" + reference, null, 200, ownerToken).path("applicationStatus").asString());
        JsonNode applications = api("GET", "/api/applications", null, 200, ownerToken);
        assertTrue(applications.isArray());
        assertEquals(1, applications.size());
        assertEquals(1, api("GET", "/api/vehicles", null, 200, ownerToken).size());
    }

    @Test
    void applicationListReturnsAllRecordsWithoutPagination() throws Exception {
        Set<String> references = new HashSet<>();
        for (int i = 0; i < 25; i++) {
            references.add(submit());
        }
        String otherToken = api("POST", "/api/auth/signup",
                owner("other-" + UUID.randomUUID() + "@example.com"), 200, null).path("accessToken").asString();
        String otherReference = api("POST", "/api/applications", vehicle(), 200, otherToken)
                .path("applicationRefNo").asString();

        JsonNode applications = api("GET", "/api/applications", null, 200, ownerToken);
        assertEquals(25, applications.size());
        for (JsonNode application : applications) {
            assertTrue(references.remove(application.path("reference").asString()));
        }
        assertTrue(references.isEmpty());

        boolean foundOther = false;
        for (JsonNode application : api("GET", "/api/applications", null, 200, officerToken)) {
            foundOther |= otherReference.equals(application.path("reference").asString());
        }
        assertTrue(foundOther);
    }

    @Test void missingRecordsHaveProperErrors() throws Exception {
        api("GET", "/api/applications/does-not-exist", null, 404, ownerToken);
        api("POST", "/api/applications/does-not-exist/review", Map.of("action", "START"), 404, officerToken);
        api("PATCH", "/api/accounts/owners/99999999", Map.of("status", "ACTIVE"), 404, systemAdminToken);
    }

    @Test void duplicateVehicleIsRejected() throws Exception {
        Map<String, Object> data = vehicle();
        api("POST", "/api/applications", data, 200, ownerToken);
        api("POST", "/api/applications", data, 409, ownerToken);
    }

    @Test void manufactureYearIsValidated() throws Exception {
        Map<String, Object> data = new HashMap<>(vehicle());
        data.put("manufactureYear", Year.now().getValue() + 1);
        api("POST", "/api/applications", data, 409, ownerToken);
    }

    @Test void correctionRequiresOfficerRequestAndResubmits() throws Exception {
        String ref = submit();
        api("PUT", "/api/applications/" + ref + "/correction", vehicle(), 409, ownerToken);
        review(ref, "START", 200);
        review(ref, "CORRECTION", 200);
        JsonNode corrected = api("PUT", "/api/applications/" + ref + "/correction", vehicle(), 200, ownerToken);
        assertEquals("SUBMITTED", corrected.path("applicationStatus").asString());
    }

    @Test void paymentChecksAmountAndDoesNotDuplicate() throws Exception {
        String ref = submit();
        api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 1, "cardLastFourDigit", "1234"), 400, ownerToken);
        JsonNode first = api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 200, ownerToken);
        JsonNode second = api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 200, ownerToken);
        assertEquals(first.path("payment").path("paymentId"), second.path("payment").path("paymentId"));
        assertEquals("SUCCESS", first.path("payment").path("paymentStatus").asString());
    }

    @Test void approvalRequiresPaymentAndPassedInspection() throws Exception {
        String ref = submit();
        assertEquals("INSPECTION_NOT_PASSED", review(ref, "APPROVE", 409).path("code").asString());
        inspected(ref);
        assertEquals("PAYMENT_REQUIRED", review(ref, "APPROVE", 409).path("code").asString());
    }

    @Test void completedWorkflowIssuesCertificateAndClosesApplication() throws Exception {
        String ref = submit();
        api("GET", "/api/applications/" + ref + "/certificate", null, 404, ownerToken);
        pay(ref);
        inspected(ref);
        JsonNode approved = review(ref, "APPROVE", 200);
        assertEquals("APPROVED", approved.path("applicationStatus").asString());
        JsonNode certificate = api("GET", "/api/applications/" + ref + "/certificate", null, 200, ownerToken);
        assertTrue(certificate.path("registrationNumber").asString().startsWith("VR"));
        assertEquals(LocalDate.now().plusYears(15).toString(), certificate.path("validTill").asString());
        review(ref, "APPROVE", 409);
    }

    @Test void rejectionPreventsPaymentAndCertificate() throws Exception {
        String ref = submit();
        assertEquals("REJECTED", review(ref, "REJECT", 200).path("applicationStatus").asString());
        api("POST", "/api/applications/" + ref + "/payments", Map.of("amount", 600, "cardLastFourDigit", "1234"), 409, ownerToken);
        api("GET", "/api/applications/" + ref + "/certificate", null, 404, ownerToken);
    }

    @Test void auditLogRecordsTheSignedInActor() throws Exception {
        String ref = submit();
        review(ref, "START", 200);
        boolean ownerSubmitted = false;
        boolean officerStarted = false;
        for (JsonNode entry : api("GET", "/api/audit", null, 200, systemAdminToken)) {
            if (!ref.equals(entry.path("applicationRefNo").asString())) continue;
            ownerSubmitted |= "SUBMITTED".equals(entry.path("action").asString())
                    && ("OWNER:" + ownerId).equals(entry.path("actor").asString());
            officerStarted |= "START".equals(entry.path("action").asString())
                    && ("RTO_OFFICER:" + officerId).equals(entry.path("actor").asString());
        }
        assertTrue(ownerSubmitted);
        assertTrue(officerStarted);
    }

    // ---------------------------------------------------------------- supporting documents

    @Test void ownerUploadsListsAndDownloadsDocuments() throws Exception {
        String ref = submitWithoutDocuments();
        JsonNode uploaded = upload(ref, "INVOICE", "C:\\fakepath\\invoice.pdf", PDF, 200, ownerToken);
        assertEquals("PENDING", uploaded.path("status").asString());
        assertEquals("invoice.pdf", uploaded.path("originalFileName").asString());
        assertEquals("application/pdf", uploaded.path("contentType").asString());
        assertEquals(PDF.length, uploaded.path("sizeBytes").asLong());
        assertFalse(uploaded.has("storageKey"));
        upload(ref, "IDENTITY_PROOF", "id.png", PNG, 200, ownerToken);

        assertEquals(2, documents(ref, ownerToken).size());
        assertEquals(2, api("GET", "/api/applications/" + ref, null, 200, ownerToken).path("documents").size());

        HttpResponse<byte[]> file = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                        + "/api/applications/" + ref + "/documents/" + uploaded.path("documentId").asLong() + "/file"))
                .header("Authorization", "Bearer " + officerToken).build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, file.statusCode());
        assertTrue(Arrays.equals(PDF, file.body()));
        assertEquals("application/pdf", file.headers().firstValue("Content-Type").orElse(""));
        assertEquals("nosniff", file.headers().firstValue("X-Content-Type-Options").orElse(""));
    }

    @Test void replacingADocumentKeepsOneCurrentCopy() throws Exception {
        String ref = submitWithoutDocuments();
        long first = upload(ref, "INSURANCE", "old.pdf", PDF, 200, ownerToken).path("documentId").asLong();
        JsonNode replaced = upload(ref, "INSURANCE", "new.png", PNG, 200, ownerToken);
        assertEquals(first, replaced.path("documentId").asLong());
        assertEquals("image/png", replaced.path("contentType").asString());
        assertEquals(1, documents(ref, ownerToken).size());
    }

    @Test void invalidFilesAreRejected() throws Exception {
        String ref = submitWithoutDocuments();
        byte[] program = "MZ this is not a pdf".getBytes(StandardCharsets.US_ASCII);
        assertEquals("INVALID_DOCUMENT", upload(ref, "INVOICE", "invoice.pdf", program, 400, ownerToken).path("code").asString());
        upload(ref, "INVOICE", "empty.pdf", new byte[0], 400, ownerToken);
        upload(ref, "PASSPORT_PHOTO", "invoice.pdf", PDF, 400, ownerToken);
        byte[] large = new byte[5 * 1024 * 1024 + 100_000];
        System.arraycopy(PDF, 0, large, 0, PDF.length);
        assertEquals("FILE_TOO_LARGE", upload(ref, "INVOICE", "large.pdf", large, 413, ownerToken).path("code").asString());
        assertEquals(0, documents(ref, ownerToken).size());
    }

    @Test void onlyTheApplicantCanUploadAndOnlyAllowedUsersCanRead() throws Exception {
        String ref = submit();
        String otherToken = api("POST", "/api/auth/signup", owner("other-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("accessToken").asString();
        upload(ref, "INVOICE", "invoice.pdf", PDF, 403, otherToken);
        upload(ref, "INVOICE", "invoice.pdf", PDF, 403, officerToken);
        api("GET", "/api/applications/" + ref + "/documents", null, 403, otherToken);
        long id = documentId(ref, "INVOICE");
        api("GET", "/api/applications/" + ref + "/documents/" + id + "/file", null, 403, otherToken);
        api("GET", "/api/applications/" + ref + "/documents/" + id + "/file", null, 401, null);
        api("POST", "/api/applications/" + ref + "/documents/" + id + "/review", Map.of("action", "VERIFY"), 403, ownerToken);
    }

    @Test void verificationNeedsAllDocumentsUploadedAndVerified() throws Exception {
        String ref = submitWithoutDocuments();
        upload(ref, "INVOICE", "invoice.pdf", PDF, 200, ownerToken);
        upload(ref, "INSURANCE", "insurance.pdf", PDF, 200, ownerToken);
        assertTrue(review(ref, "START", 409).path("detail").asString().contains("identity proof"));
        upload(ref, "IDENTITY_PROOF", "id.pdf", PDF, 200, ownerToken);
        review(ref, "START", 200);
        reviewDocument(ref, "INVOICE", "VERIFY", null, 200);
        assertTrue(review(ref, "VERIFY", 409).path("detail").asString().contains("insurance"));
        reviewDocument(ref, "INSURANCE", "VERIFY", null, 200);
        reviewDocument(ref, "IDENTITY_PROOF", "VERIFY", null, 200);
        assertEquals("INSPECTION_PENDING", review(ref, "VERIFY", 200).path("applicationStatus").asString());
    }

    @Test void rejectedDocumentMustBeReplacedBeforeResubmission() throws Exception {
        String ref = submit();
        reviewDocument(ref, "INVOICE", "VERIFY", null, 409);
        review(ref, "START", 200);
        upload(ref, "INSURANCE", "late.pdf", PDF, 409, ownerToken);
        reviewDocument(ref, "INSURANCE", "REJECT", null, 409);
        JsonNode rejected = reviewDocument(ref, "INSURANCE", "REJECT", "Policy has expired", 200);
        assertEquals("REJECTED", rejected.path("status").asString());
        assertEquals("Policy has expired", rejected.path("remarks").asString());
        reviewDocument(ref, "INVOICE", "VERIFY", null, 200);
        review(ref, "CORRECTION", 200);

        api("PUT", "/api/applications/" + ref + "/correction", vehicle(), 409, ownerToken);
        upload(ref, "INVOICE", "invoice.pdf", PDF, 409, ownerToken);
        assertEquals("PENDING", upload(ref, "INSURANCE", "renewed.pdf", PDF, 200, ownerToken).path("status").asString());
        assertEquals("SUBMITTED", api("PUT", "/api/applications/" + ref + "/correction", vehicle(), 200, ownerToken)
                .path("applicationStatus").asString());

        review(ref, "START", 200);
        reviewDocument(ref, "INSURANCE", "VERIFY", null, 200);
        reviewDocument(ref, "IDENTITY_PROOF", "VERIFY", null, 200);
        assertEquals("INSPECTION_PENDING", review(ref, "VERIFY", 200).path("applicationStatus").asString());

        boolean auditedRejection = false;
        for (JsonNode entry : api("GET", "/api/audit", null, 200, systemAdminToken)) {
            auditedRejection |= ref.equals(entry.path("applicationRefNo").asString())
                    && "DOCUMENT_REJECTED".equals(entry.path("action").asString())
                    && ("RTO_OFFICER:" + officerId).equals(entry.path("actor").asString());
        }
        assertTrue(auditedRejection);
    }

    // ---------------------------------------------------------------- bulk actions, updates and deletes

    private JsonNode bulk(String method, String path, Object body, String token) throws Exception {
        return api(method, path, body, 200, token);
    }

    private JsonNode item(JsonNode result, String id) {
        for (JsonNode item : result.path("results")) {
            if (id.equals(item.path("id").asString())) return item;
        }
        throw new AssertionError("No result for " + id + " in " + result);
    }

    @Test void bulkReviewProcessesEachApplicationOnItsOwn() throws Exception {
        String a = submit();
        String b = submit();
        String noDocuments = submitWithoutDocuments();
        JsonNode result = bulk("POST", "/api/applications/bulk/review",
                Map.of("references", java.util.List.of(a, b, noDocuments, "VRS-MISSING", a), "action", "START"), officerToken);
        assertEquals(4, result.path("requested").asInt());
        assertEquals(2, result.path("succeeded").asInt());
        assertEquals(2, result.path("failed").asInt());
        assertEquals("UNDER_VERIFICATION", item(result, a).path("status").asString());
        assertEquals("INVALID_OPERATION", item(result, noDocuments).path("code").asString());
        assertEquals("APPLICATION_ID_NOT_FOUND", item(result, "VRS-MISSING").path("code").asString());
        assertEquals("UNDER_VERIFICATION", api("GET", "/api/applications/" + b, null, 200, ownerToken).path("applicationStatus").asString());
        assertEquals("SUBMITTED", api("GET", "/api/applications/" + noDocuments, null, 200, ownerToken).path("applicationStatus").asString());

        JsonNode rejected = bulk("POST", "/api/applications/bulk/review",
                Map.of("references", java.util.List.of(a, b), "action", "REJECT"), officerToken);
        assertEquals(0, rejected.path("succeeded").asInt());
        api("POST", "/api/applications/bulk/review", Map.of("references", java.util.List.of(a), "action", "APPROVE"), 400, officerToken);
        api("POST", "/api/applications/bulk/review", Map.of("references", java.util.List.of(a), "action", "START"), 403, ownerToken);
    }

    @Test void bulkAccountUpdateReportsEachAccount() throws Exception {
        long other = api("POST", "/api/auth/signup", owner("bulk-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("user").path("id").asLong();
        JsonNode result = bulk("PATCH", "/api/accounts/owners/bulk",
                Map.of("ids", java.util.List.of(ownerId, other, 99999999L), "status", "SUSPENDED"), systemAdminToken);
        assertEquals(2, result.path("succeeded").asInt());
        assertEquals("OWNER_NOT_FOUND", item(result, "99999999").path("code").asString());
        api("GET", "/api/applications", null, 401, ownerToken);

        long adminId = api("GET", "/api/auth/me", null, 200, systemAdminToken).path("id").asLong();
        JsonNode employees = bulk("PATCH", "/api/accounts/employees/bulk",
                Map.of("ids", java.util.List.of(officerId, adminId), "status", "INACTIVE"), systemAdminToken);
        assertTrue(item(employees, String.valueOf(officerId)).path("success").asBoolean());
        assertFalse(item(employees, String.valueOf(adminId)).path("success").asBoolean());
        api("PATCH", "/api/accounts/owners/bulk", Map.of("ids", java.util.List.of(ownerId), "status", "ACTIVE"), 403, rtoAdminToken);
    }

    @Test void onlyUnusedFeeRulesCanBeDeleted() throws Exception {
        submit();
        long unused = api("POST", "/api/fees", Map.of("vehicleCategory", "LMV", "applicationType", "NEW",
                "feeAmount", 900, "effectiveFrom", LocalDate.now().plusDays(30).toString()), 200, rtoAdminToken).path("feeRuleId").asLong();
        long used = -1;
        for (JsonNode fee : api("GET", "/api/fees", null, 200, rtoAdminToken)) {
            if ("PRIVATE_CAR".equals(fee.path("vehicleCategory").asString())) used = Math.max(used, fee.path("feeRuleId").asLong());
        }
        JsonNode result = bulk("POST", "/api/fees/bulk/delete", Map.of("ids", java.util.List.of(unused, used)), rtoAdminToken);
        assertTrue(item(result, String.valueOf(unused)).path("success").asBoolean());
        assertEquals("INVALID_OPERATION", item(result, String.valueOf(used)).path("code").asString());
        api("POST", "/api/fees/bulk/delete", Map.of("ids", java.util.List.of(used)), 403, officerToken);
    }

    @Test void ownersWithdrawOnlyTheirOwnUnpaidApplications() throws Exception {
        String unpaid = submit();
        String paid = submit();
        pay(paid);
        String underReview = submit();
        review(underReview, "START", 200);
        String otherToken = api("POST", "/api/auth/signup", owner("other-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("accessToken").asString();
        String others = api("POST", "/api/applications", vehicle(), 200, otherToken).path("applicationRefNo").asString();

        JsonNode result = bulk("POST", "/api/applications/bulk/withdraw",
                Map.of("references", java.util.List.of(unpaid, paid, underReview, others)), ownerToken);
        assertEquals(1, result.path("succeeded").asInt());
        assertEquals("WITHDRAWN", item(result, unpaid).path("status").asString());
        assertEquals("ACCESS_DENIED", item(result, others).path("code").asString());
        api("GET", "/api/applications/" + unpaid, null, 404, ownerToken);
        api("GET", "/api/applications/" + paid, null, 200, ownerToken);
        assertEquals(2, api("GET", "/api/vehicles", null, 200, ownerToken).size());
        api("POST", "/api/applications/bulk/withdraw", Map.of("references", java.util.List.of(paid)), 403, officerToken);
    }

    // ---------------------------------------------------------------- CSV export and import

    private HttpResponse<byte[]> download(String path, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Authorization", "Bearer " + token).build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private String text(HttpResponse<byte[]> response) {
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    private HttpResponse<String> uploadCsv(String path, String csv, String token) throws Exception {
        String boundary = "----vrs" + UUID.randomUUID();
        String body = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"data.csv\"\r\n"
                + "Content-Type: text/csv\r\n\r\n" + csv + "\r\n--" + boundary + "--\r\n";
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode importCsv(String path, String csv, int status, String token) throws Exception {
        HttpResponse<String> response = uploadCsv(path, csv, token);
        assertEquals(status, response.statusCode(), path + ": " + response.body());
        return json.readTree(response.body());
    }

    @Test void csvExportsAreScopedMaskedAndSafe() throws Exception {
        String mine = submit();
        String otherToken = api("POST", "/api/auth/signup", owner("other-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("accessToken").asString();
        String others = api("POST", "/api/applications", vehicle(), 200, otherToken).path("applicationRefNo").asString();

        HttpResponse<byte[]> export = download("/api/export/applications.csv", ownerToken);
        assertEquals(200, export.statusCode());
        assertTrue(export.headers().firstValue("Content-Type").orElse("").startsWith("text/csv"));
        assertTrue(export.headers().firstValue("Content-Disposition").orElse("").contains("attachment"));
        String csv = text(export);
        assertTrue(csv.startsWith("\uFEFFreference,type,status"));
        assertTrue(csv.contains(mine));
        assertFalse(csv.contains(others));
        assertTrue(text(download("/api/export/applications.csv", officerToken)).contains(others));
        String approvedOnly = text(download("/api/export/applications.csv?status=APPROVED", officerToken));
        assertFalse(approvedOnly.contains(mine));
        for (String line : approvedOnly.split("\r\n")) {
            assertTrue(line.startsWith("﻿") || line.contains(",APPROVED,"), line);
        }

        Map<String, Object> risky = new HashMap<>(owner("formula-" + UUID.randomUUID() + "@example.com"));
        risky.put("firstName", "=HYPERLINK(1)");
        risky.put("identityProofNumber", "ABCD12345678");
        api("POST", "/api/auth/signup", risky, 200, null);
        String owners = text(download("/api/export/owners.csv", systemAdminToken));
        assertTrue(owners.contains("'=HYPERLINK(1)"));
        assertTrue(owners.contains("********5678"));
        assertFalse(owners.contains("ABCD12345678"));
        assertEquals(403, download("/api/export/owners.csv", rtoAdminToken).statusCode());
        assertEquals(403, download("/api/export/employees.csv", officerToken).statusCode());
        assertFalse(text(download("/api/export/employees.csv", rtoAdminToken)).contains("password"));
    }

    @Test void feeImportChecksEveryRowBeforeSaving() throws Exception {
        String bad = "vehicleCategory,applicationType,feeAmount,effectiveFrom\r\n"
                + "TWO_WHEELER,NEW,300,2030-01-01\r\n"
                + "SPACESHIP,NEW,abc,2030-01-01\r\n"
                + "three_wheeler,RENEWAL,400,2030-01-01\r\n";
        JsonNode dry = importCsv("/api/import/fees", bad, 200, rtoAdminToken);
        assertTrue(dry.path("dryRun").asBoolean());
        assertEquals(3, dry.path("totalRows").asInt());
        assertEquals(1, dry.path("validRows").asInt());
        assertEquals(0, dry.path("imported").asInt());
        assertTrue(dry.path("errors").toString().contains("vehicleCategory"));
        assertTrue(dry.path("errors").toString().contains("feeAmount"));
        assertEquals(0, importCsv("/api/import/fees?dryRun=false", bad, 200, rtoAdminToken).path("imported").asInt());

        String good = "feeRuleId,vehicleCategory,applicationType,feeAmount,effectiveFrom\r\n"
                + "1,TWO_WHEELER,NEW,350,2031-01-01\r\n"
                + "2,three_wheeler,new,450.50,2031-01-01\r\n";
        assertEquals(2, importCsv("/api/import/fees?dryRun=false", good, 200, rtoAdminToken).path("imported").asInt());
        String fees = api("GET", "/api/fees", null, 200, rtoAdminToken).toString();
        assertTrue(fees.contains("450.5"));

        assertEquals("INVALID_REQUEST", importCsv("/api/import/fees", "category,amount\r\nLMV,1\r\n", 400, rtoAdminToken).path("code").asString());
        importCsv("/api/import/fees", good, 403, officerToken);
    }

    @Test void employeeImportFollowsCreationRules() throws Exception {
        String email = "import-" + UUID.randomUUID() + "@example.com";
        String csv = "firstName,lastName,emailAddress,phoneNumber,password,designation,role\r\n"
                + "Ravi,Kumar," + email + ",9876543212," + PASSWORD + ",CLERK,RTO_OFFICER\r\n"
                + "Meena,Iyer,admin-" + UUID.randomUUID() + "@example.com,9876543213," + PASSWORD + ",ADMIN,SYSTEM_ADMIN\r\n"
                + "Dup,User," + email + ",9876543214," + PASSWORD + ",CLERK,RTO_OFFICER\r\n"
                + "Short,Pwd,short-" + UUID.randomUUID() + "@example.com,9876543215,short,CLERK,RTO_OFFICER\r\n";
        JsonNode dry = importCsv("/api/import/employees", csv, 200, rtoAdminToken);
        assertEquals(1, dry.path("validRows").asInt());
        String errors = dry.path("errors").toString();
        assertTrue(errors.contains("\"row\":3") && errors.contains("role"));
        assertTrue(errors.contains("\"row\":4") && errors.contains("more than once"));
        assertTrue(errors.contains("\"row\":5") && errors.contains("password"));

        String single = "firstName,lastName,emailAddress,phoneNumber,password,designation,role\r\n"
                + "Ravi,Kumar," + email + ",9876543212," + PASSWORD + ",CLERK,RTO_OFFICER\r\n";
        assertEquals(1, importCsv("/api/import/employees?dryRun=false", single, 200, rtoAdminToken).path("imported").asInt());
        assertEquals("RTO_OFFICER", api("GET", "/api/auth/me", null, 200, login(email, PASSWORD, true)).path("role").asString());
        assertTrue(importCsv("/api/import/employees", single, 200, systemAdminToken).path("errors").toString().contains("already exists"));
    }

    // ---------------------------------------------------------------- PDF downloads

    @Test void certificateReceiptAndReportPdfs() throws Exception {
        String ref = submit();
        assertEquals(404, download("/api/applications/" + ref + "/receipt.pdf", ownerToken).statusCode());
        pay(ref);
        HttpResponse<byte[]> receipt = download("/api/applications/" + ref + "/receipt.pdf", ownerToken);
        assertEquals(200, receipt.statusCode());
        assertEquals("application/pdf", receipt.headers().firstValue("Content-Type").orElse(""));
        assertTrue(text(receipt).startsWith("%PDF-"));
        assertEquals(404, download("/api/applications/" + ref + "/certificate.pdf", ownerToken).statusCode());

        inspected(ref);
        review(ref, "APPROVE", 200);
        HttpResponse<byte[]> rc = download("/api/applications/" + ref + "/certificate.pdf", ownerToken);
        assertEquals(200, rc.statusCode());
        assertTrue(text(rc).startsWith("%PDF-"));
        assertTrue(rc.headers().firstValue("Content-Disposition").orElse("").contains("RC-VR"));
        assertEquals(200, download("/api/applications/" + ref + "/certificate.pdf", officerToken).statusCode());

        String otherToken = api("POST", "/api/auth/signup", owner("other-" + UUID.randomUUID() + "@example.com"), 200, null)
                .path("accessToken").asString();
        assertEquals(403, download("/api/applications/" + ref + "/certificate.pdf", otherToken).statusCode());

        HttpResponse<byte[]> report = download("/api/reports/applications.pdf", rtoAdminToken);
        assertEquals(200, report.statusCode());
        assertTrue(text(report).startsWith("%PDF-"));
        assertEquals(403, download("/api/reports/applications.pdf", officerToken).statusCode());
    }

    // ---------------------------------------------------------------- administration

    @Test void administratorsManageFeesAndAccounts() throws Exception {
        api("GET", "/api/owners", null, 200, systemAdminToken);
        api("GET", "/api/employees", null, 200, rtoAdminToken);
        api("GET", "/api/fees", null, 200, ownerToken);
        api("GET", "/api/reports", null, 200, rtoAdminToken);
        api("GET", "/api/audit", null, 200, systemAdminToken);
        api("PATCH", "/api/accounts/owners/" + ownerId, Map.of("status", "INACTIVE"), 200, systemAdminToken);
        api("PATCH", "/api/accounts/employees/" + officerId, Map.of("status", "ACTIVE", "role", "RTO_ADMIN"), 200, systemAdminToken);
        api("PATCH", "/api/accounts/owners/" + ownerId, Map.of("status", "ACTIVE", "role", "RTO_ADMIN"), 409, systemAdminToken);
        api("POST", "/api/fees", Map.of("vehicleCategory", "PRIVATE_CAR", "applicationType", "RENEWAL", "feeAmount", 600,
                "effectiveFrom", LocalDate.now().toString()), 409, rtoAdminToken);
    }

    @Test void corsAndMalformedRequestsAreHandled() throws Exception {
        HttpResponse<String> allowed = raw("OPTIONS", "/api/applications", null, Map.of("Origin", "http://localhost:3000",
                "Access-Control-Request-Method", "POST", "Access-Control-Request-Headers", "authorization,content-type"));
        assertEquals(200, allowed.statusCode());
        assertEquals("http://localhost:3000", allowed.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertEquals(403, raw("OPTIONS", "/api/applications", null, Map.of("Origin", "https://unrelated.example",
                "Access-Control-Request-Method", "POST")).statusCode());
        assertEquals(400, raw("POST", "/api/auth/signup", "{bad", Map.of()).statusCode());
    }

    @Test void deferredEndpointsAreAbsentAndHealthIsPublic() throws Exception {
        for (String path : Set.of("/api/applications/renewal", "/api/applications/transfer")) {
            api("GET", path, null, 404, ownerToken);
        }
        api("GET", "/actuator/health", null, 200, null);
    }
}
