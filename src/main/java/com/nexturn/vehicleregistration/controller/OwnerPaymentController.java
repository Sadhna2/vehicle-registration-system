package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class OwnerPaymentController {

    private final VehicleRegistrationApplicationService applicationService;
    private final Access access;

    public OwnerPaymentController(
            VehicleRegistrationApplicationService applicationService,
            Access access) {

        this.applicationService = applicationService;
        this.access = access;
    }

    @PostMapping("/{ref}/payments")
    public ApplicationDetailsResponse pay(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody PaymentRequest request) {

        return applicationService.pay(
                referenceNumber,
                request,
                access.actor(requestInfo));
    }
}