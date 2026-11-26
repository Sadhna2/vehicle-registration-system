package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class RegistrationCertificateController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    @GetMapping("/{ref}/certificate")
    public RegistrationCertificateResponse certificate(Actor actor,
            @PathVariable("ref") String referenceNumber) {
        return applicationService.certificate(referenceNumber, ownerScope(actor));
    }
}
