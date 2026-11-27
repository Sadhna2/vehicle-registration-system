package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PDF downloads: registration certificate, payment receipt and the department report. */
@RestController
@RequestMapping("/api")
public class PdfController {
    @Autowired
    private PdfService pdfService;

    @GetMapping("/applications/{ref}/certificate.pdf")
    public ResponseEntity<byte[]> certificate(Actor actor, @PathVariable("ref") String referenceNumber) {
        return pdf(pdfService.certificate(referenceNumber, ownerScope(actor)));
    }

    @GetMapping("/applications/{ref}/receipt.pdf")
    public ResponseEntity<byte[]> receipt(Actor actor, @PathVariable("ref") String referenceNumber) {
        return pdf(pdfService.receipt(referenceNumber, ownerScope(actor)));
    }

    @GetMapping("/reports/applications.pdf")
    @Allow({SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN})
    public ResponseEntity<byte[]> report() {
        return pdf(pdfService.applicationsReport());
    }

    private static ResponseEntity<byte[]> pdf(PdfService.PdfFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.content());
    }
}
