package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;

import com.nexturn.vehicleregistration.dto.request.DocumentReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDocumentResponse;
import com.nexturn.vehicleregistration.entity.ApplicationDocument;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.DocumentStatus;
import com.nexturn.vehicleregistration.enums.DocumentType;
import com.nexturn.vehicleregistration.exception.ApplicationIdNotFoundException;
import com.nexturn.vehicleregistration.exception.DocumentNotFoundException;
import com.nexturn.vehicleregistration.exception.EmployeeNotFoundException;
import com.nexturn.vehicleregistration.exception.InvalidDocumentException;
import com.nexturn.vehicleregistration.exception.ReferenceNumberNotFoundException;
import com.nexturn.vehicleregistration.repository.ApplicationDocumentRepository;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.security.AccessRules;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.DocumentService;
import com.nexturn.vehicleregistration.service.DocumentStorage;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    /** Accepted file formats, recognised by their first bytes rather than the file name. */
    private enum FileFormat {
        PDF("application/pdf", "pdf", new byte[] {'%', 'P', 'D', 'F', '-'}),
        PNG("image/png", "png", new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}),
        JPEG("image/jpeg", "jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        final String contentType;
        final String extension;
        final byte[] signature;

        FileFormat(String contentType, String extension, byte[] signature) {
            this.contentType = contentType;
            this.extension = extension;
            this.signature = signature;
        }

        static FileFormat detect(byte[] content) {
            for (FileFormat format : values()) {
                if (content.length >= format.signature.length
                        && Arrays.equals(content, 0, format.signature.length,
                                format.signature, 0, format.signature.length)) {
                    return format;
                }
            }
            return null;
        }
    }

    private static final Set<ApplicationStatus> UPLOAD_ALLOWED =
            EnumSet.of(ApplicationStatus.SUBMITTED, ApplicationStatus.CORRECTION_REQUIRED);

    @Autowired
    private ApplicationDocumentRepository documentRepository;

    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Autowired
    private DocumentStorage storage;

    @Autowired
    private AuditService auditService;

    @Value("${vrs.documents.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDocumentResponse> list(String referenceNumber, Long ownerId) {
        VehicleRegistrationApplication application = applicationRepository
                .findById(referenceNumber)
                .orElseThrow(() -> new ReferenceNumberNotFoundException(referenceNumber));
        AccessRules.requireReadable(application, ownerId);
        return documentRepository
                .findByApplicationApplicationRefNoOrderByDocumentTypeAsc(referenceNumber)
                .stream()
                .map(ApplicationDocumentResponse::from)
                .toList();
    }

    @Override
    public ApplicationDocumentResponse upload(
            String referenceNumber, DocumentType type, MultipartFile file, Long ownerId) {
        VehicleRegistrationApplication application = applicationRepository
                .locked(referenceNumber)
                .orElseThrow(() -> new ApplicationIdNotFoundException(referenceNumber));
        AccessRules.requireApplicant(application, ownerId);
        ensure(
                UPLOAD_ALLOWED.contains(application.getApplicationStatus()),
                "Documents can only be uploaded while the application is submitted"
                        + " or awaiting correction");
        if (type == null) {
            throw new InvalidDocumentException("Choose the document type");
        }

        byte[] content = readContent(file);
        FileFormat format = FileFormat.detect(content);
        if (format == null) {
            throw new InvalidDocumentException("Only PDF, JPG and PNG files are accepted");
        }

        ApplicationDocument document = documentRepository
                .findByApplicationApplicationRefNoAndDocumentType(referenceNumber, type)
                .orElse(null);
        ensure(
                document == null || document.getStatus() != DocumentStatus.VERIFIED,
                "This document is already verified and cannot be replaced");
        boolean replacing = document != null;
        String previousKey = replacing ? document.getStorageKey() : null;
        if (!replacing) {
            document = new ApplicationDocument();
            document.setApplication(application);
            document.setDocumentType(type);
        }

        String key = storage.store(referenceNumber, format.extension, content);
        cleanUpFilesAfterTransaction(key, previousKey);

        document.setStorageKey(key);
        document.setOriginalFileName(cleanFileName(file.getOriginalFilename(), format));
        document.setContentType(format.contentType);
        document.setSizeBytes(content.length);
        document.setSha256(sha256(content));
        document.setStatus(DocumentStatus.PENDING);
        document.setRemarks(null);
        document.setVerifiedBy(null);
        document.setVerifiedAt(null);
        document.setUploadedAt(Instant.now());
        ApplicationDocument saved = documentRepository.save(document);

        application.setUpdatedDate(Instant.now());
        auditService.record(
                application,
                replacing ? "DOCUMENT_REPLACED" : "DOCUMENT_UPLOADED",
                type.name());
        return ApplicationDocumentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentFile download(String referenceNumber, Long documentId, Long ownerId) {
        ApplicationDocument document = documentRepository
                .findByDocumentIdAndApplicationApplicationRefNo(documentId, referenceNumber)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));
        AccessRules.requireReadable(document.getApplication(), ownerId);
        return new DocumentFile(
                storage.load(document.getStorageKey()),
                document.getContentType(),
                document.getOriginalFileName(),
                document.getSizeBytes());
    }

    @Override
    public ApplicationDocumentResponse review(
            String referenceNumber, Long documentId, DocumentReviewRequest request, Long employeeId) {
        VehicleRegistrationApplication application = applicationRepository
                .locked(referenceNumber)
                .orElseThrow(() -> new ApplicationIdNotFoundException(referenceNumber));
        ensure(
                application.getApplicationStatus() == ApplicationStatus.UNDER_VERIFICATION,
                "Start verification before reviewing documents");
        ApplicationDocument document = documentRepository
                .findByDocumentIdAndApplicationApplicationRefNo(documentId, referenceNumber)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));
        RTOEmployee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        String remarks = request.remarks() == null ? null : request.remarks().trim();
        boolean verify = "VERIFY".equals(request.action());
        ensure(
                verify || (remarks != null && !remarks.isEmpty()),
                "Explain why the document is rejected so the owner can fix it");

        document.setStatus(verify ? DocumentStatus.VERIFIED : DocumentStatus.REJECTED);
        document.setRemarks(remarks == null || remarks.isEmpty() ? null : remarks);
        document.setVerifiedBy(employee);
        document.setVerifiedAt(Instant.now());
        application.setUpdatedDate(Instant.now());

        auditService.record(
                application,
                verify ? "DOCUMENT_VERIFIED" : "DOCUMENT_REJECTED",
                document.getDocumentType().name() + (remarks == null ? "" : ": " + remarks));
        return ApplicationDocumentResponse.from(document);
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAllUploaded(String referenceNumber) {
        Set<DocumentType> missing = EnumSet.allOf(DocumentType.class);
        documents(referenceNumber).forEach(document -> missing.remove(document.getDocumentType()));
        ensure(missing.isEmpty(), "Required documents are missing: " + names(missing));
    }

    @Override
    @Transactional(readOnly = true)
    public void requireAllVerified(String referenceNumber) {
        Set<DocumentType> unverified = EnumSet.allOf(DocumentType.class);
        documents(referenceNumber).stream()
                .filter(document -> document.getStatus() == DocumentStatus.VERIFIED)
                .forEach(document -> unverified.remove(document.getDocumentType()));
        ensure(unverified.isEmpty(), "Verify every document first. Not verified: " + names(unverified));
    }

    @Override
    @Transactional(readOnly = true)
    public void requireNoneRejected(String referenceNumber) {
        Set<DocumentType> rejected = documents(referenceNumber).stream()
                .filter(document -> document.getStatus() == DocumentStatus.REJECTED)
                .map(ApplicationDocument::getDocumentType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DocumentType.class)));
        ensure(rejected.isEmpty(), "Upload a replacement for the rejected documents: " + names(rejected));
    }

    @Override
    public void deleteAllFor(String referenceNumber) {
        List<ApplicationDocument> documents = documents(referenceNumber);
        List<String> keys = documents.stream().map(ApplicationDocument::getStorageKey).toList();
        documentRepository.deleteAll(documents);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(storage::delete);
            }
        });
    }

    private List<ApplicationDocument> documents(String referenceNumber) {
        return documentRepository.findByApplicationApplicationRefNoOrderByDocumentTypeAsc(referenceNumber);
    }

    private byte[] readContent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("Choose a file to upload");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidDocumentException(
                    "The file is larger than " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
        try {
            return file.getBytes();
        } catch (IOException failure) {
            throw new InvalidDocumentException("The file could not be read. Please try again.");
        }
    }

    /**
     * Keeps disk and database consistent: if the transaction rolls back, the newly written file is
     * removed; if it commits, the file it replaced is removed.
     */
    private void cleanUpFilesAfterTransaction(String newKey, String replacedKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    if (replacedKey != null) {
                        storage.delete(replacedKey);
                    }
                } else {
                    storage.delete(newKey);
                }
            }
        });
    }

    /** Keeps only a safe display name: no folders, control characters or quotes. */
    private static String cleanFileName(String original, FileFormat format) {
        String name = original == null ? "" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}\"<>|:*?]", "")
                .trim();
        if (name.isEmpty()) {
            name = "document." + format.extension;
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static String names(Set<DocumentType> types) {
        return types.stream()
                .map(type -> type.name().replace('_', ' ').toLowerCase())
                .collect(Collectors.joining(", "));
    }
}
