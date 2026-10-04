package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fees")
public class RegistrationFeeRuleController {

    private final Access access;
    private final AdministrationService administrationService;

    public RegistrationFeeRuleController(
            Access access,
            AdministrationService administrationService) {

        this.access = access;
        this.administrationService = administrationService;
    }

    @GetMapping
    public List<RegistrationFeeRuleResponse> fees(
            @ApiRequest RequestInfo requestInfo) {

        access.actor(requestInfo);

        return administrationService.fees();
    }

    @PostMapping
    public RegistrationFeeRuleResponse fee(
            @ApiRequest RequestInfo requestInfo,
            @Valid @RequestBody RegistrationFeeRuleRequest request) {

        return administrationService.fee(
                request, access.actor(requestInfo));
    }
}
