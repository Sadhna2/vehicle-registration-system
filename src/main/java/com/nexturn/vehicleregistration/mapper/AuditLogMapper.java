package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.entity.AuditLogs;

public final class AuditLogMapper {
  private AuditLogMapper() {}

  public static AuditLogResponse toResponse(AuditLogs a) {
    return new AuditLogResponse(
        a.getId(),
        a.getActor(),
        a.getAction(),
        a.getApplicationRefNo(),
        a.getRemarks(),
        a.getCreatedAt());
  }
}
