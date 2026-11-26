package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    /** Owners see their own vehicles; staff see all vehicles. */
    @GetMapping
    public List<VehicleResponse> vehicles(Actor actor) {
        return applicationService.vehicles(ownerScope(actor));
    }
}
