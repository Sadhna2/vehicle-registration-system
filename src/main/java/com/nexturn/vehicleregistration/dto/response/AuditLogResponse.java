package com.nexturn.vehicleregistration.dto.response;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String actor,
    String action,
    String applicationRefNo,
    String remarks,
    Instant createdAt) {}

