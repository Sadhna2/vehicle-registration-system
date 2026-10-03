package com.nexturn.vehicleregistration.entity;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.FinalResult;
import com.nexturn.vehicleregistration.enums.InspectionStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_registration_applications")
public class VehicleRegistrationApplication {
  @Id
  @Column(length = 30)
  private String applicationRefNo;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "applicant_id")
  private Owner applicant;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationType applicationType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fee_rule_id")
  private RegistrationFeeRule feeRule;

  @Column(precision = 10, scale = 2, nullable = false)
  private java.math.BigDecimal payableAmount;

  private java.time.LocalDate previousValidUntil;

  @Column(nullable = false)
  private java.time.Instant submittedDate = java.time.Instant.now();

  @Column(nullable = false)
  private java.time.Instant updatedDate = java.time.Instant.now();

  @Column(length = 500)
  private String verificationRemark;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verified_by")
  private RTOEmployee verifiedBy;

  private java.time.Instant verifiedAt;
  private java.time.LocalDate inspectionScheduleDate;

  @Column(length = 500)
  private String inspectionRemark;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inspected_by")
  private RTOEmployee inspectedBy;

  private java.time.Instant inspectedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "decided_by")
  private RTOEmployee decidedBy;

  private java.time.Instant decidedAt;

  @Column(length = 500)
  private String decisionRemarks;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InspectionStatus inspectionStatus = InspectionStatus.PENDING;

  @Enumerated(EnumType.STRING)
  private FinalResult finalResult;

  public FinalResult getFinalResult() {
    return finalResult;
  }

  public void setFinalResult(FinalResult value) {
    finalResult = value;
  }

  @Version private long version;

  public String getApplicationRefNo() {
    return applicationRefNo;
  }

  public void setApplicationRefNo(String value) {
    this.applicationRefNo = value;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle value) {
    this.vehicle = value;
  }

  public Owner getApplicant() {
    return applicant;
  }

  public void setApplicant(Owner value) {
    this.applicant = value;
  }

  public ApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(ApplicationType value) {
    this.applicationType = value;
  }

  public ApplicationStatus getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(ApplicationStatus value) {
    this.applicationStatus = value;
  }

  public RegistrationFeeRule getFeeRule() {
    return feeRule;
  }

  public void setFeeRule(RegistrationFeeRule value) {
    this.feeRule = value;
  }

  public java.math.BigDecimal getPayableAmount() {
    return payableAmount;
  }

  public void setPayableAmount(java.math.BigDecimal value) {
    this.payableAmount = value;
  }

  public java.time.LocalDate getPreviousValidUntil() {
    return previousValidUntil;
  }

  public void setPreviousValidUntil(java.time.LocalDate value) {
    this.previousValidUntil = value;
  }

  public java.time.Instant getSubmittedDate() {
    return submittedDate;
  }

  public void setSubmittedDate(java.time.Instant value) {
    this.submittedDate = value;
  }

  public java.time.Instant getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(java.time.Instant value) {
    this.updatedDate = value;
  }

  public String getVerificationRemark() {
    return verificationRemark;
  }

  public void setVerificationRemark(String value) {
    this.verificationRemark = value;
  }

  public RTOEmployee getVerifiedBy() {
    return verifiedBy;
  }

  public void setVerifiedBy(RTOEmployee value) {
    this.verifiedBy = value;
  }

  public java.time.Instant getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(java.time.Instant value) {
    this.verifiedAt = value;
  }

  public java.time.LocalDate getInspectionScheduleDate() {
    return inspectionScheduleDate;
  }

  public void setInspectionScheduleDate(java.time.LocalDate value) {
    this.inspectionScheduleDate = value;
  }

  public String getInspectionRemark() {
    return inspectionRemark;
  }

  public void setInspectionRemark(String value) {
    this.inspectionRemark = value;
  }

  public RTOEmployee getInspectedBy() {
    return inspectedBy;
  }

  public void setInspectedBy(RTOEmployee value) {
    this.inspectedBy = value;
  }

  public java.time.Instant getInspectedAt() {
    return inspectedAt;
  }

  public void setInspectedAt(java.time.Instant value) {
    this.inspectedAt = value;
  }

  public RTOEmployee getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(RTOEmployee value) {
    this.decidedBy = value;
  }

  public java.time.Instant getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(java.time.Instant value) {
    this.decidedAt = value;
  }

  public String getDecisionRemarks() {
    return decisionRemarks;
  }

  public void setDecisionRemarks(String value) {
    this.decisionRemarks = value;
  }

  public InspectionStatus getInspectionStatus() {
    return inspectionStatus;
  }

  public void setInspectionStatus(InspectionStatus value) {
    this.inspectionStatus = value;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    this.version = value;
  }
}

