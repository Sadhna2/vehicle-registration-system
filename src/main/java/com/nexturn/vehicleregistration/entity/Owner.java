package com.nexturn.vehicleregistration.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.IdentityProofType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "owner_details")
public class Owner {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ownerId;

  @Column(nullable = false, length = 30)
  private String firstName;

  @Column(nullable = false, length = 30)
  private String lastName;

  @Column(nullable = false, unique = true, length = 100)
  private String emailAddress;

  @Column(nullable = false, length = 15)
  private String phoneNumber;

  @JsonIgnore
  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false)
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IdentityProofType identityProofType;

  @Column(nullable = false, unique = true, length = 30)
  private String identityProofNumber;

  @Column(nullable = false, length = 150)
  private String address;

  @Column(nullable = false, length = 50)
  private String cityName;

  @Column(nullable = false, length = 50)
  private String stateName;

  @Column(nullable = false, length = 6)
  private String pincode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(nullable = false)
  private Instant dateOfCreation = Instant.now();

  @Column(nullable = false)
  private Instant dateOfUpdate = Instant.now();

  public Long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Long value) {
    this.ownerId = value;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String value) {
    this.firstName = value;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String value) {
    this.lastName = value;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String value) {
    this.emailAddress = value;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String value) {
    this.phoneNumber = value;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    this.password = value;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth; 
  }

  public void setDateOfBirth(LocalDate value) {
    this.dateOfBirth = value;
  }

  public IdentityProofType getIdentityProofType() {
    return identityProofType;
  }

  public void setIdentityProofType(IdentityProofType value) {
    this.identityProofType = value;
  }

  public String getIdentityProofNumber() {
    return identityProofNumber;
  }

  public void setIdentityProofNumber(String value) {
    this.identityProofNumber = value;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String value) {
    this.cityName = value;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String value) {
    this.stateName = value;
  }

  public String getPincode() {
    return pincode;
  }

  public void setPincode(String value) {
    this.pincode = value;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus value) {
    this.status = value;
  }

  public Instant getDateOfCreation() {
    return dateOfCreation;
  }

  public void setDateOfCreation(Instant value) {
    this.dateOfCreation = value;
  }

  public Instant getDateOfUpdate() {
    return dateOfUpdate;
  }

  public void setDateOfUpdate(Instant value) {
    this.dateOfUpdate = value;
  }
}

