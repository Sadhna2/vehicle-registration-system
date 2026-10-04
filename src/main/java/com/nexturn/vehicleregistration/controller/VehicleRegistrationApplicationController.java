package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationPageResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class VehicleRegistrationApplicationController {

    private final VehicleRegistrationApplicationService applicationService;
    private final Access access;

    public VehicleRegistrationApplicationController(
            VehicleRegistrationApplicationService applicationService,
            Access access) {

        this.applicationService = applicationService;
        this.access = access;
    }

    @GetMapping
    public ApplicationPageResponse list(
            @ApiRequest RequestInfo requestInfo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        return applicationService.list(
                access.actor(requestInfo), page, size);
    }

    @PostMapping
    public ApplicationDetailsResponse submit(
            @ApiRequest RequestInfo requestInfo,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {

        return applicationService.submit(
                request, access.actor(requestInfo));
    }

    @GetMapping("/{ref}")
    public ApplicationDetailsResponse get(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("ref") String referenceNumber) {

        return applicationService.get(
                referenceNumber, access.actor(requestInfo));
    }

    @PutMapping("/{ref}/correction")
    public ApplicationDetailsResponse correct(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {

        return applicationService.correct(
                referenceNumber,
                request,
                access.actor(requestInfo));
    }

    @PostMapping("/{ref}/review")
    public ApplicationDetailsResponse review(
            @ApiRequest RequestInfo requestInfo,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody ApplicationReviewRequest request) {

        return applicationService.review(
                referenceNumber,
                request,
                access.actor(requestInfo));
    }
}