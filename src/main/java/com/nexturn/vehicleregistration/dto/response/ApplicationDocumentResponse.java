package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.entity.ApplicationDocument;
import com.nexturn.vehicleregistration.enums.DocumentStatus;
import com.nexturn.vehicleregistration.enums.DocumentType;
import java.time.Instant;

/** Document details shown to owners and officers. The storage location is never exposed. */
public record ApplicationDocumentResponse(
        Long documentId,
        DocumentType documentType,
        String originalFileName,
        String contentType,
        long sizeBytes,
        DocumentStatus status,
        String remarks,
        Long verifiedByEmployeeId,
        Instant verifiedAt,
        Instant uploadedAt) {

    public static ApplicationDocumentResponse from(ApplicationDocument document) {
        return new ApplicationDocumentResponse(
                document.getDocumentId(),
                document.getDocumentType(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getStatus(),
                document.getRemarks(),
                document.getVerifiedBy() == null ? null : document.getVerifiedBy().getEmployeeId(),
                document.getVerifiedAt(),
                document.getUploadedAt());
    }
}
