package com.nexturn.vehicleregistration.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
  private LocalDate oldValidTill;

  @Column(nullable = false)
  private LocalDate newValidTill;

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

  public LocalDate getOldValidTill() {
    return oldValidTill;
  }

  public void setOldValidTill(LocalDate value) {
    this.oldValidTill = value;
  }

  public LocalDate getNewValidTill() {
    return newValidTill;
  }

  public void setNewValidTill(LocalDate value) {
    this.newValidTill = value;
  }
}
