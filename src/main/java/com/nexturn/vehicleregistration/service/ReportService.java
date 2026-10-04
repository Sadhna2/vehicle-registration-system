package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;

import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> report(Actor actor);

    List<AuditLogResponse> audits(Actor actor);
}
