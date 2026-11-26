package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class OwnerPaymentController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    @PostMapping("/{ref}/payments")
    @Allow(SessionRole.OWNER)
    public ApplicationDetailsResponse pay(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody PaymentRequest request) {
        return applicationService.pay(referenceNumber, request, actor.id());
    }
}
