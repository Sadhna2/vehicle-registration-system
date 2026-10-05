package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/applications")
public class RegistrationCertificateController {
    private final VehicleRegistrationApplicationService applicationService;

    public RegistrationCertificateController(VehicleRegistrationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{ref}/certificate")
    public RegistrationCertificateResponse certificate(@PathVariable("ref") String referenceNumber) {
        return applicationService.certificate(referenceNumber);
    }
}
