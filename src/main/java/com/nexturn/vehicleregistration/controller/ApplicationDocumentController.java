package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import com.nexturn.vehicleregistration.dto.request.DocumentReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDocumentResponse;
import com.nexturn.vehicleregistration.enums.DocumentType;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Upload, view and verify the supporting documents of an application (FR-1, FR-3). */
@RestController
@RequestMapping("/api/applications/{ref}/documents")
public class ApplicationDocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping
    public List<ApplicationDocumentResponse> list(Actor actor, @PathVariable("ref") String referenceNumber) {
        return documentService.list(referenceNumber, ownerScope(actor));
    }

    /** Multipart upload: {@code type} (INVOICE, INSURANCE, IDENTITY_PROOF) and {@code file}. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Allow(SessionRole.OWNER)
    public ApplicationDocumentResponse upload(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @RequestParam("type") DocumentType type,
            @RequestPart("file") MultipartFile file) {
        return documentService.upload(referenceNumber, type, file, actor.id());
    }

    /** Streams the stored file to the owner or an RTO employee. */
    @GetMapping("/{documentId}/file")
    public ResponseEntity<Resource> file(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @PathVariable("documentId") Long documentId) {
        DocumentService.DocumentFile file =
                documentService.download(referenceNumber, documentId, ownerScope(actor));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.fileName(),
                                java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox")
                .body(file.content());
    }

    @PostMapping("/{documentId}/review")
    @Allow({SessionRole.RTO_OFFICER, SessionRole.RTO_ADMIN})
    public ApplicationDocumentResponse review(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @PathVariable("documentId") Long documentId,
            @Valid @RequestBody DocumentReviewRequest request) {
        return documentService.review(referenceNumber, documentId, request, actor.id());
    }
}
