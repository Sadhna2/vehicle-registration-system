package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/applications")
public class OwnerPaymentController {
    private final VehicleRegistrationApplicationService applicationService;

    public OwnerPaymentController(VehicleRegistrationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/{ref}/payments")
    public ApplicationDetailsResponse pay(@PathVariable("ref") String referenceNumber,
            @Valid @RequestBody PaymentRequest request) {
        return applicationService.pay(referenceNumber, request);
    }
}
