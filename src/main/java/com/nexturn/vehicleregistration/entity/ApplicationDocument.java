package com.nexturn.vehicleregistration.entity;

import com.nexturn.vehicleregistration.enums.DocumentStatus;
import com.nexturn.vehicleregistration.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * One supporting document for an application. The file itself is kept in document storage under
 * {@code storageKey}; only its details are stored here. Each application has at most one current
 * document of each type: uploading again replaces it.
 */
@Entity
@Table(
    name = "application_documents",
    uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "document_type"}))
public class ApplicationDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long documentId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id")
  private VehicleRegistrationApplication application;

  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false, length = 20)
  private DocumentType documentType;

  @Column(nullable = false, length = 255)
  private String originalFileName;

  @Column(nullable = false, unique = true, length = 120)
  private String storageKey;

  @Column(nullable = false, length = 50)
  private String contentType;

  @Column(nullable = false)
  private long sizeBytes;

  @Column(nullable = false, length = 64)
  private String sha256;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DocumentStatus status = DocumentStatus.PENDING;

  @Column(length = 500)
  private String remarks;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verified_by")
  private RTOEmployee verifiedBy;

  private Instant verifiedAt;

  @Column(nullable = false)
  private Instant uploadedAt = Instant.now();

  @Version private long version;

  public Long getDocumentId() {
    return documentId;
  }

  public void setDocumentId(Long value) {
    this.documentId = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType value) {
    this.documentType = value;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String value) {
    this.originalFileName = value;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String value) {
    this.storageKey = value;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String value) {
    this.contentType = value;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(long value) {
    this.sizeBytes = value;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String value) {
    this.sha256 = value;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus value) {
    this.status = value;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String value) {
    this.remarks = value;
  }

  public RTOEmployee getVerifiedBy() {
    return verifiedBy;
  }

  public void setVerifiedBy(RTOEmployee value) {
    this.verifiedBy = value;
  }

  public Instant getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(Instant value) {
    this.verifiedAt = value;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(Instant value) {
    this.uploadedAt = value;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    this.version = value;
  }
}
