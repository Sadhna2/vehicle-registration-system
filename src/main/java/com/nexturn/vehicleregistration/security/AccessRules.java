package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.exception.AccessDeniedException;

/**
 * Ownership checks shared by the services. Call inside a transaction. {@code ownerId} comes from
 * the caller's verified token: the owner's id, or {@code null} for RTO staff.
 */
public final class AccessRules {

  private AccessRules() {}

  /** The owner id to scope queries by: the caller's id for owners, null (everything) for staff. */
  public static Long ownerScope(Actor actor) {
    return actor.isOwner() ? actor.id() : null;
  }

  /** Owners may only see their own applications; staff (ownerId == null) may see all. */
  public static void requireReadable(VehicleRegistrationApplication application, Long ownerId) {
    if (ownerId != null) {
      requireApplicant(application, ownerId);
    }
  }

  /** Only the owner who submitted the application may change it. */
  public static void requireApplicant(VehicleRegistrationApplication application, Long ownerId) {
    if (ownerId == null || !application.getApplicant().getOwnerId().equals(ownerId)) {
      throw new AccessDeniedException("This application belongs to another owner");
    }
  }
}
