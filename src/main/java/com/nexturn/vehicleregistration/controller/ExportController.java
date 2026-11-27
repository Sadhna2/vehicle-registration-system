package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.ExportService;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** CSV downloads. Owners export only their own records; staff exports are role-restricted. */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    @Autowired
    private ExportService exportService;

    @GetMapping("/applications.csv")
    public ResponseEntity<byte[]> applications(Actor actor,
            @RequestParam(name = "status", required = false) ApplicationStatus status) {
        return csv(exportService.applications(ownerScope(actor), status));
    }

    @GetMapping("/vehicles.csv")
    public ResponseEntity<byte[]> vehicles(Actor actor) {
        return csv(exportService.vehicles(ownerScope(actor)));
    }

    @GetMapping("/owners.csv")
    @Allow(SessionRole.SYSTEM_ADMIN)
    public ResponseEntity<byte[]> owners() {
        return csv(exportService.owners());
    }

    @GetMapping("/employees.csv")
    @Allow({SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN})
    public ResponseEntity<byte[]> employees() {
        return csv(exportService.employees());
    }

    @GetMapping("/fees.csv")
    @Allow({SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN})
    public ResponseEntity<byte[]> fees() {
        return csv(exportService.feeRules());
    }

    private static ResponseEntity<byte[]> csv(ExportService.CsvFile file) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .body(file.content());
    }
}
