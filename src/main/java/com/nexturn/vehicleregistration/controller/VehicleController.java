package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRegistrationApplicationService applicationService;
    private final Access access;

    public VehicleController(
            VehicleRegistrationApplicationService applicationService,
            Access access) {

        this.applicationService = applicationService;
        this.access = access;
    }

    @GetMapping
    public List<VehicleResponse> vehicles(
            @ApiRequest RequestInfo requestInfo) {

        return applicationService.vehicles(access.actor(requestInfo));
    }
}