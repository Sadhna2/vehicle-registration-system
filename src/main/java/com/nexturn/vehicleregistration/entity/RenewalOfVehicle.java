package com.nexturn.vehicleregistration.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "renewal_of_vehicle")
public class RenewalOfVehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long renewalId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", unique = true)
  private VehicleRegistrationApplication application;

  @Column(nullable = false)
  private java.time.LocalDate oldValidTill;

  @Column(nullable = false)
  private java.time.LocalDate newValidTill;

  public Long getRenewalId() {
    return renewalId;
  }

  public void setRenewalId(Long value) {
    this.renewalId = value;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle value) {
    this.vehicle = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public java.time.LocalDate getOldValidTill() {
    return oldValidTill;
  }

  public void setOldValidTill(java.time.LocalDate value) {
    this.oldValidTill = value;
  }

  public java.time.LocalDate getNewValidTill() {
    return newValidTill;
  }

  public void setNewValidTill(java.time.LocalDate value) {
    this.newValidTill = value;
  }
}

