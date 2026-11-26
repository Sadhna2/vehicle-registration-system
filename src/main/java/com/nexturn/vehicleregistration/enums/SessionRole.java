package com.nexturn.vehicleregistration.enums;

/** Role carried in the JWT. Owners are OWNER; employees carry their RTOEmployeeRole name. */
public enum SessionRole {
  OWNER,
  RTO_OFFICER,
  RTO_ADMIN,
  SYSTEM_ADMIN;

  public static SessionRole of(RTOEmployeeRole role) {
    return SessionRole.valueOf(role.name());
  }
}
