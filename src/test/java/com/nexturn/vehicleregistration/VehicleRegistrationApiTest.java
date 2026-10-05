package com.nexturn.vehicleregistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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
    private String email;
    private long ownerId;
    private long employeeId;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> "jdbc:h2:mem:registration-tests;MODE=MySQL;DB_CLOSE_DELAY=-1");
        properties.add("spring.datasource.username", () -> "sa");
        properties.add("spring.datasource.password", () -> "");
        properties.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        properties.add("spring.jpa.show-sql", () -> "false");
        properties.add("spring.config.import", () -> "");
    }

    private HttpResponse<String> raw(String method, String path, String body, Map<String,String> headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        headers.forEach(request::header);
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode api(String method, String path, Object body, int status) throws Exception {
        HttpResponse<String> response = raw(method, path, body == null ? null : json.writeValueAsString(body), Map.of());
        assertEquals(status, response.statusCode(), method + " " + path + ": " + response.body());
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    private Map<String,Object> owner(String accountEmail) {
        return Map.ofEntries(Map.entry("firstName","Test"),Map.entry("lastName","Owner"),
                Map.entry("emailAddress",accountEmail),Map.entry("phoneNumber","9876543210"),
                Map.entry("password",PASSWORD),Map.entry("dateOfBirth","1995-01-01"),
                Map.entry("identityProofType","PAN"),Map.entry("identityProofNumber",UUID.randomUUID().toString().substring(0,20)),
                Map.entry("address","12 Test Road"),Map.entry("cityName","Pune"),
                Map.entry("stateName","Maharashtra"),Map.entry("pincode","411001"));
    }

    private Map<String,Object> vehicle() {
        return Map.of("vehicleCategory","PRIVATE_CAR","manufacturerName","Maruti Suzuki", "modelName","Swift",
                "chassisNumber", "CH"+UUID.randomUUID(), "engineNumber", "EN"+UUID.randomUUID(),
                "fuelType","PETROL","manufactureYear",Year.now().getValue(),"colorVariant","White");
    }

    @BeforeEach
    void createRecordsWithoutAuthorization() throws Exception {
        email = "owner-"+UUID.randomUUID()+"@example.com";
        ownerId=api("POST","/api/auth/signup",owner(email),200).path("id").asLong();
        employeeId=api("POST","/api/employees",Map.of("firstName","Test","lastName","Officer",
                "emailAddress","staff-"+UUID.randomUUID()+"@example.com","phoneNumber","9876543211",
                "password",PASSWORD,"designation","OFFICER","role","RTO_OFFICER"),200).path("employeeId").asLong();
        api("POST","/api/fees",Map.of("vehicleCategory","PRIVATE_CAR","applicationType","NEW",
                "feeAmount",600,"effectiveFrom",LocalDate.now().toString()),200);
    }

    private String submit() throws Exception {
        return api("POST","/api/applications?ownerId="+ownerId,vehicle(),200).path("applicationRefNo").asString();
    }

    private JsonNode review(String reference,String action,int status) throws Exception {
        return api("POST","/api/applications/"+reference+"/review?employeeId="+employeeId,
                Map.of("action",action,"remarks","Test review","appointment",LocalDate.now().toString()),status);
    }

    private void inspected(String reference) throws Exception {
        review(reference,"START",200);review(reference,"VERIFY",200);
        review(reference,"SCHEDULE",200);review(reference,"PASS",200);
    }

    @Test void ownerLoginReturnsOnlyUserDetails() throws Exception {
        JsonNode result=api("POST","/api/auth/login",Map.of("email",email,"password",PASSWORD,"staff",false),200);
        assertEquals(ownerId,result.path("id").asLong());assertEquals("OWNER",result.path("role").asString());
        assertFalse(result.has("accessToken"));assertFalse(result.has("password"));
        HttpResponse<String> response=raw("POST","/api/auth/login",json.writeValueAsString(Map.of("email",email,"password",PASSWORD,"staff",false)),Map.of());
        assertTrue(response.headers().allValues("Set-Cookie").isEmpty());
        assertNotEquals(PASSWORD,owners.findById(ownerId).orElseThrow().getPassword());
        assertTrue(PasswordUtil.matches(PASSWORD,owners.findById(ownerId).orElseThrow().getPassword()));
    }

    @Test void employeeLoginUsesStoredCredentials() throws Exception {
        JsonNode employees=api("GET","/api/employees",null,200);
        String staffEmail="";for(JsonNode employee:employees)if(employee.path("employeeId").asLong()==employeeId)staffEmail=employee.path("emailAddress").asString();
        JsonNode result=api("POST","/api/auth/login",Map.of("email",staffEmail,"password",PASSWORD,"staff",true),200);
        assertEquals(employeeId,result.path("id").asLong());assertEquals("RTO_OFFICER",result.path("role").asString());
    }

    @Test void incorrectCredentialsAreRejected() throws Exception {
        assertEquals("INVALID_LOGIN",api("POST","/api/auth/login",Map.of("email",email,"password","WrongPassword123!","staff",false),401).path("code").asString());
    }

    @Test void invalidOwnerFieldsReturnValidationErrors() throws Exception {
        assertEquals("VALIDATION_FAILED",api("POST","/api/auth/signup",Map.of(),400).path("code").asString());
        api("POST","/api/auth/login",Map.of(),400);
    }

    @Test void duplicateOwnerIsRejected() throws Exception {api("POST","/api/auth/signup",owner(email),409);}

    @Test void submissionAndQueriesNeedNoToken() throws Exception {
        String reference=submit();assertTrue(reference.startsWith("VRS-"));
        assertEquals("SUBMITTED",api("GET","/api/applications/"+reference,null,200).path("applicationStatus").asString());
        JsonNode applications=api("GET","/api/applications?ownerId="+ownerId,null,200);
        assertTrue(applications.isArray());assertEquals(1,applications.size());
        assertEquals(1,api("GET","/api/vehicles?ownerId="+ownerId,null,200).size());
        api("GET","/api/applications",null,200);api("GET","/api/vehicles",null,200);
    }

    @Test
    void applicationListReturnsAllRecordsWithoutPagination() throws Exception {
        Set<String> references = new HashSet<>();
        for (int i = 0; i < 25; i++) {
            references.add(submit());
        }
        long otherOwnerId = api("POST", "/api/auth/signup",
                owner("other-" + UUID.randomUUID() + "@example.com"), 200)
                .path("id").asLong();
        String otherReference = api("POST", "/api/applications?ownerId=" + otherOwnerId,
                vehicle(), 200).path("applicationRefNo").asString();
        JsonNode applications = api("GET", "/api/applications?ownerId=" + ownerId, null, 200);
        assertTrue(applications.isArray());
        assertEquals(25, applications.size());
        for (JsonNode application : applications) {
            assertTrue(references.remove(application.path("reference").asString()));
        }
        assertTrue(references.isEmpty());
        JsonNode allApplications = api("GET", "/api/applications", null, 200);
        assertTrue(allApplications.isArray());
        boolean foundOther = false;
        for (JsonNode application : allApplications) {
            foundOther |= otherReference.equals(application.path("reference").asString());
        }
        assertTrue(foundOther);
        JsonNode parameters = api("GET", "/v3/api-docs", null, 200)
                .path("paths").path("/api/applications").path("get").path("parameters");
        for (JsonNode parameter : parameters) {
            assertFalse(Set.of("page", "size").contains(parameter.path("name").asString()));
        }
    }

    @Test void missingIdsAndMissingRecordsHaveProperErrors() throws Exception {
        api("POST","/api/applications",vehicle(),400);
        assertEquals("OWNER_NOT_FOUND",api("POST","/api/applications?ownerId=99999999",vehicle(),404).path("code").asString());
        api("GET","/api/applications/does-not-exist",null,404);
        String ref=submit();api("POST","/api/applications/"+ref+"/review",Map.of("action","START"),400);
        api("POST","/api/applications/"+ref+"/review?employeeId=99999999",Map.of("action","START"),404);
    }

    @Test void duplicateVehicleIsRejected() throws Exception {
        Map<String,Object> data=vehicle();api("POST","/api/applications?ownerId="+ownerId,data,200);
        api("POST","/api/applications?ownerId="+ownerId,data,409);
    }

    @Test void manufactureYearIsValidated() throws Exception {
        Map<String,Object> data=new HashMap<>(vehicle());data.put("manufactureYear",Year.now().getValue()+1);
        api("POST","/api/applications?ownerId="+ownerId,data,409);
    }

    @Test void correctionRequiresOfficerRequestAndResubmits() throws Exception {
        String ref=submit();api("PUT","/api/applications/"+ref+"/correction",vehicle(),409);
        review(ref,"START",200);review(ref,"CORRECTION",200);
        JsonNode corrected=api("PUT","/api/applications/"+ref+"/correction",vehicle(),200);
        assertEquals("SUBMITTED",corrected.path("applicationStatus").asString());
    }

    @Test void paymentChecksAmountAndDoesNotDuplicate() throws Exception {
        String ref=submit();api("POST","/api/applications/"+ref+"/payments",Map.of("amount",1,"cardLastFourDigit","1234"),400);
        JsonNode first=api("POST","/api/applications/"+ref+"/payments",Map.of("amount",600,"cardLastFourDigit","1234"),200);
        JsonNode second=api("POST","/api/applications/"+ref+"/payments",Map.of("amount",600,"cardLastFourDigit","1234"),200);
        assertEquals(first.path("payment").path("paymentId"),second.path("payment").path("paymentId"));
        assertEquals("SUCCESS",first.path("payment").path("paymentStatus").asString());
    }

    @Test void approvalRequiresPaymentAndPassedInspection() throws Exception {
        String ref=submit();assertEquals("INSPECTION_NOT_PASSED",review(ref,"APPROVE",409).path("code").asString());
        inspected(ref);assertEquals("PAYMENT_REQUIRED",review(ref,"APPROVE",409).path("code").asString());
    }

    @Test void completedWorkflowIssuesCertificateAndClosesApplication() throws Exception {
        String ref=submit();api("GET","/api/applications/"+ref+"/certificate",null,404);
        api("POST","/api/applications/"+ref+"/payments",Map.of("amount",600,"cardLastFourDigit","1234"),200);
        inspected(ref);JsonNode approved=review(ref,"APPROVE",200);
        assertEquals("APPROVED",approved.path("applicationStatus").asString());
        JsonNode certificate=api("GET","/api/applications/"+ref+"/certificate",null,200);
        assertTrue(certificate.path("registrationNumber").asString().startsWith("VR"));
        assertEquals(LocalDate.now().plusYears(15).toString(),certificate.path("validTill").asString());
        review(ref,"APPROVE",409);
    }

    @Test void rejectionPreventsPaymentAndCertificate() throws Exception {
        String ref=submit();assertEquals("REJECTED",review(ref,"REJECT",200).path("applicationStatus").asString());
        api("POST","/api/applications/"+ref+"/payments",Map.of("amount",600,"cardLastFourDigit","1234"),409);
        api("GET","/api/applications/"+ref+"/certificate",null,404);
    }

    @Test void administratorsAndFeesArePublicBusinessEndpoints() throws Exception {
        api("GET","/api/owners",null,200);api("GET","/api/employees",null,200);api("GET","/api/fees",null,200);
        api("GET","/api/reports",null,200);api("GET","/api/audit",null,200);
        api("PATCH","/api/accounts/owners/"+ownerId,Map.of("status","INACTIVE"),200);
        api("PATCH","/api/accounts/employees/"+employeeId,Map.of("status","ACTIVE","role","RTO_ADMIN"),200);
        api("PATCH","/api/accounts/owners/99999999",Map.of("status","ACTIVE"),404);
        api("PATCH","/api/accounts/owners/"+ownerId,Map.of("status","ACTIVE","role","RTO_ADMIN"),409);
        api("POST","/api/fees",Map.of("vehicleCategory","PRIVATE_CAR","applicationType","RENEWAL","feeAmount",600,"effectiveFrom",LocalDate.now().toString()),409);
    }

    @Test void corsAndMalformedRequestsAreHandled() throws Exception {
        HttpResponse<String> allowed=raw("OPTIONS","/api/applications",null,Map.of("Origin","http://localhost:5173","Access-Control-Request-Method","POST","Access-Control-Request-Headers","content-type"));
        assertEquals(200,allowed.statusCode());assertEquals("http://localhost:5173",allowed.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertEquals(403,raw("OPTIONS","/api/applications",null,Map.of("Origin","https://unrelated.example","Access-Control-Request-Method","POST")).statusCode());
        assertEquals(400,raw("POST","/api/auth/signup","{bad",Map.of()).statusCode());
    }

    @Test void removedSecurityAndDeferredEndpointsAreAbsent() throws Exception {
        for(String path:Set.of("/api/auth/me","/api/auth/logout","/api/applications/renewal","/api/applications/transfer","/api/documents")) api(path.endsWith("logout") ? "POST" : "GET",path,null,404);
        JsonNode document=api("GET","/v3/api-docs",null,200);assertFalse(document.path("components").has("securitySchemes"));
        api("GET","/actuator/health",null,200);
    }
}
