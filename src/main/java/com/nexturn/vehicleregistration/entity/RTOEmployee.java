package com.nexturn.vehicleregistration.entity;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;

import jakarta.persistence.*;

@Entity
@Table(name = "rta_employees")
public class RTOEmployee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long employeeId;

  @Column(nullable = false, length = 30)
  private String firstName;

  @Column(nullable = false, length = 30)
  private String lastName;

  @Column(nullable = false, unique = true, length = 100)
  private String emailAddress;

  @Column(nullable = false, length = 15)
  private String phoneNumber;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "desgnation", nullable = false)
  private RTOEmployeeDesignation designation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RTOEmployeeRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(nullable = false)
  private java.time.Instant dateOfJoining = java.time.Instant.now();

  public Long getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(Long value) {
    this.employeeId = value;
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

  public RTOEmployeeDesignation getDesignation() {
    return designation;
  }

  public void setDesignation(RTOEmployeeDesignation value) {
    this.designation = value;
  }

  public RTOEmployeeRole getRole() {
    return role;
  }

  public void setRole(RTOEmployeeRole value) {
    this.role = value;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus value) {
    this.status = value;
  }

  public java.time.Instant getDateOfJoining() {
    return dateOfJoining;
  }

  public void setDateOfJoining(java.time.Instant value) {
    this.dateOfJoining = value;
  }
}

