package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.DocumentReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDocumentResponse;
import com.nexturn.vehicleregistration.enums.DocumentType;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Supporting documents (invoice, insurance, identity proof) for an application. {@code ownerId}
 * is the calling owner's id from the JWT, or {@code null} for RTO staff.
 */
public interface DocumentService {

    List<ApplicationDocumentResponse> list(String referenceNumber, Long ownerId);

    ApplicationDocumentResponse upload(
            String referenceNumber, DocumentType type, MultipartFile file, Long ownerId);

    DocumentFile download(String referenceNumber, Long documentId, Long ownerId);

    ApplicationDocumentResponse review(
            String referenceNumber, Long documentId, DocumentReviewRequest request, Long employeeId);

    /** Throws unless every required document type has been uploaded. */
    void requireAllUploaded(String referenceNumber);

    /** Throws unless every required document has been verified by an officer. */
    void requireAllVerified(String referenceNumber);

    /** Throws if the owner still has a rejected document to replace. */
    void requireNoneRejected(String referenceNumber);

    /** Deletes every document of an application; files are removed once the change commits. */
    void deleteAllFor(String referenceNumber);

    record DocumentFile(Resource content, String contentType, String fileName, long sizeBytes) {}
}
