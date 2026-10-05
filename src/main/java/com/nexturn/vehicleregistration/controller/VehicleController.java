package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleRegistrationApplicationService applicationService;

    public VehicleController(VehicleRegistrationApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<VehicleResponse> vehicles(@RequestParam(name="ownerId", required=false) Long ownerId) {
        return applicationService.vehicles(ownerId);
    }
}
