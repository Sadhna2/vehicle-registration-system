package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AdministrationController {

    private final Access access;
    private final AdministrationService administrationService;

    public AdministrationController(
            Access access,
            AdministrationService administrationService) {

        this.access = access;
        this.administrationService = administrationService;
    }

    @GetMapping("/reports")
    public Map<String, Object> report(
            @ApiRequest RequestInfo requestInfo) {

        return administrationService.report(access.actor(requestInfo));
    }

    @GetMapping("/audit")
    public List<AuditLogResponse> audit(
            @ApiRequest RequestInfo requestInfo) {

        return administrationService.audits(access.actor(requestInfo));
    }

    @GetMapping("/employees")
    public List<RTOEmployeeResponse> employees(
            @ApiRequest RequestInfo requestInfo) {

        return administrationService.employees(access.actor(requestInfo));
    }

    @PostMapping("/employees")
    public RTOEmployeeResponse employee(
            @ApiRequest RequestInfo requestInfo,
            @Valid @RequestBody RTOEmployeeRequest request) {

        return administrationService.employee(
                request, access.actor(requestInfo));
    }

    @GetMapping("/owners")
    public List<OwnerResponse> owners(
            @ApiRequest RequestInfo requestInfo) {

        return administrationService.owners(access.actor(requestInfo));
    }

    @PatchMapping("/accounts/{kind}/{id}")
    public void account(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("kind") String accountType,
            @PathVariable("id") Long accountId,
            @Valid @RequestBody AccountRequest request) {

        administrationService.account(
                accountType,
                accountId,
                request,
                access.actor(requestInfo));
    }
}
