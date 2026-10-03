package com.nexturn.vehicleregistration.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "audit_logs")
public class AuditLogs {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String actor;
  private String action;
  private String applicationRefNo;

  @Column(length = 500)
  private String remarks;

  private java.time.Instant createdAt = java.time.Instant.now();

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public String getActor() {
    return actor;
  }

  public void setActor(String value) {
    this.actor = value;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String value) {
    this.action = value;
  }

  public String getApplicationRefNo() {
    return applicationRefNo;
  }

  public void setApplicationRefNo(String value) {
    this.applicationRefNo = value;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String value) {
    this.remarks = value;
  }

  public java.time.Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(java.time.Instant value) {
    this.createdAt = value;
  }
}
