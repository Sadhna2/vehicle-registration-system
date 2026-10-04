package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;
import com.nexturn.vehicleregistration.service.ReportService;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class AdministrationServiceImpl implements AdministrationService {

    private final RegistrationFeeRuleService registrationFeeRuleService;
    private final RTOEmployeeService employeeService;
    private final ReportService reportService;

    public AdministrationServiceImpl(
            RegistrationFeeRuleService registrationFeeRuleService,
            RTOEmployeeService employeeService,
            ReportService reportService) {

        this.registrationFeeRuleService = registrationFeeRuleService;
        this.employeeService = employeeService;
        this.reportService = reportService;
    }

    @Override
    public List<RegistrationFeeRuleResponse> fees() {
        return registrationFeeRuleService.fees();
    }

    @Override
    public RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request, Actor actor) {

        return registrationFeeRuleService.fee(request, actor);
    }

    @Override
    public List<RTOEmployeeResponse> employees(Actor actor) {
        return employeeService.employees(actor);
    }

    @Override
    public RTOEmployeeResponse employee(
            RTOEmployeeRequest request, Actor actor) {

        return employeeService.employee(request, actor);
    }

    @Override
    public List<OwnerResponse> owners(Actor actor) {
        return employeeService.owners(actor);
    }

    @Override
    public void account(
            String accountType,
            Long accountId,
            AccountRequest request,
            Actor actor) {

        employeeService.account(accountType, accountId, request, actor);
    }

    @Override
    public Map<String, Object> report(Actor actor) {
        return reportService.report(actor);
    }

    @Override
    public List<AuditLogResponse> audits(Actor actor) {
        return reportService.audits(actor);
    }
}
