package com.nexturn.vehicleregistration.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_certificate")
public class RegistrationCertificate {
  @Id
  @Column(length = 20)
  private String registrationNumber;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "temporaryregistration_no", unique = true)
  private VehicleRegistrationApplication application;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "issued_by")
  private RTOEmployee issuedBy;

  @Column(nullable = false)
  private java.time.LocalDate issuedDate;

  @Column(nullable = false)
  private java.time.LocalDate validTill;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "register_owner_id")
  private Owner registeredOwner;

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String value) {
    this.registrationNumber = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public RTOEmployee getIssuedBy() {
    return issuedBy;
  }

  public void setIssuedBy(RTOEmployee value) {
    this.issuedBy = value;
  }

  public java.time.LocalDate getIssuedDate() {
    return issuedDate;
  }

  public void setIssuedDate(java.time.LocalDate value) {
    this.issuedDate = value;
  }

  public java.time.LocalDate getValidTill() {
    return validTill;
  }

  public void setValidTill(java.time.LocalDate value) {
    this.validTill = value;
  }

  public Owner getRegisteredOwner() {
    return registeredOwner;
  }

  public void setRegisteredOwner(Owner value) {
    this.registeredOwner = value;
  }
}

