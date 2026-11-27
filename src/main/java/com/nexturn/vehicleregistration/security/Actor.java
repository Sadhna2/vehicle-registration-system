package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.enums.SessionRole;

/** The authenticated caller, taken from a verified JWT. */
public record Actor(Long id, SessionRole role, String name) {

  public boolean isOwner() {
    return role == SessionRole.OWNER;
  }

  public boolean isStaff() {
    return role != SessionRole.OWNER;
  }

  public String label() {
    return role.name() + ":" + id;
  }
}
