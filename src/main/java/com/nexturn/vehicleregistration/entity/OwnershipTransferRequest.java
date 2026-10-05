package com.nexturn.vehicleregistration.entity;

import java.time.Instant;

import com.nexturn.vehicleregistration.enums.TransferStatus;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ownership_transfer_request")
public class OwnershipTransferRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transferId;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", unique = true)
  private VehicleRegistrationApplication application;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "current_owner_id")
  private Owner currentOwner;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "new_owner_id")
  private Owner newOwner;

  @Column(nullable = false)
  private boolean currentOwnerConsent;

  @Column(nullable = false)
  private boolean newOwnerConsent;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferStatus status = TransferStatus.PENDING;

  @Column(nullable = false)
  private Instant requestedAt = Instant.now();

  @Column(length = 500)
  private String remark;

  public Long getTransferId() {
    return transferId;
  }

  public void setTransferId(Long value) {
    this.transferId = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle value) {
    this.vehicle = value;
  }

  public Owner getCurrentOwner() {
    return currentOwner;
  }

  public void setCurrentOwner(Owner value) {
    this.currentOwner = value;
  }

  public Owner getNewOwner() {
    return newOwner;
  }

  public void setNewOwner(Owner value) {
    this.newOwner = value;
  }

  public boolean getCurrentOwnerConsent() {
    return currentOwnerConsent;
  }

  public void setCurrentOwnerConsent(boolean value) {
    this.currentOwnerConsent = value;
  }

  public boolean getNewOwnerConsent() {
    return newOwnerConsent;
  }

  public void setNewOwnerConsent(boolean value) {
    this.newOwnerConsent = value;
  }

  public TransferStatus getStatus() {
    return status;
  }

  public void setStatus(TransferStatus value) {
    this.status = value;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(Instant value) {
    this.requestedAt = value;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String value) {
    this.remark = value;
  }
}
