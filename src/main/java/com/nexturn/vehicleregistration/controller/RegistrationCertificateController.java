package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class RegistrationCertificateController {

    private final VehicleRegistrationApplicationService applicationService;
    private final Access access;

    public RegistrationCertificateController(
            VehicleRegistrationApplicationService applicationService,
            Access access) {

        this.applicationService = applicationService;
        this.access = access;
    }

    @GetMapping("/{ref}/certificate")
    public RegistrationCertificateResponse certificate(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("ref") String referenceNumber) {

        return applicationService.certificate(
                referenceNumber, access.actor(requestInfo));
    }
}