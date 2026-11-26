package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;

import com.nexturn.vehicleregistration.enums.SessionRole;
import java.util.List;
import java.util.Map;

public interface AdministrationService {
    List<RegistrationFeeRuleResponse> fees();

    RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request);

    List<RTOEmployeeResponse> employees();

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request, SessionRole creatorRole);

    List<OwnerResponse> owners();

    void account(
            String accountType,
            Long accountId,
            AccountRequest request, Long actingEmployeeId);

    Map<String, Object> report();

    List<AuditLogResponse> audits();
}
