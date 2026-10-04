package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;

import java.util.List;
import java.util.Map;

public interface AdministrationService {

    List<RegistrationFeeRuleResponse> fees();

    RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request, Actor actor);

    List<RTOEmployeeResponse> employees(Actor actor);

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request, Actor actor);

    List<OwnerResponse> owners(Actor actor);

    void account(
            String accountType,
            Long accountId,
            AccountRequest request,
            Actor actor);

    Map<String, Object> report(Actor actor);

    List<AuditLogResponse> audits(Actor actor);
}
