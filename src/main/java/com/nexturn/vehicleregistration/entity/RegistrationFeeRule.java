package com.nexturn.vehicleregistration.entity;

import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_fee_rules")
public class RegistrationFeeRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long feeRuleId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VehicleCategory vehicleCategory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationType applicationType;

  @Column(precision = 10, scale = 2, nullable = false)
  private java.math.BigDecimal feeAmount;

  @Column(nullable = false)
  private java.time.LocalDate effectiveFrom;

  public Long getFeeRuleId() {
    return feeRuleId;
  }

  public void setFeeRuleId(Long value) {
    this.feeRuleId = value;
  }

  public VehicleCategory getVehicleCategory() {
    return vehicleCategory;
  }

  public void setVehicleCategory(VehicleCategory value) {
    this.vehicleCategory = value;
  }

  public ApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(ApplicationType value) {
    this.applicationType = value;
  }

  public java.math.BigDecimal getFeeAmount() {
    return feeAmount;
  }

  public void setFeeAmount(java.math.BigDecimal value) {
    this.feeAmount = value;
  }

  public java.time.LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(java.time.LocalDate value) {
    this.effectiveFrom = value;
  }
}

