package com.nexturn.vehicleregistration.controller;

import static com.nexturn.vehicleregistration.security.AccessRules.ownerScope;

import java.util.List;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private VehicleRegistrationApplicationService applicationService;

    /**
     * Owners see their own applications; staff (including the system admin) see all of them.
     * Optional {@code status} (e.g. SUBMITTED, APPROVED) returns only applications in that state.
     */
    @GetMapping
    public List<ApplicationSummaryResponse> list(Actor actor,
            @RequestParam(name = "status", required = false) ApplicationStatus status) {
        return applicationService.list(ownerScope(actor), status);
    }

    @PostMapping
    @Allow(SessionRole.OWNER)
    public ApplicationDetailsResponse submit(Actor actor,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {
        return applicationService.submit(request, actor.id());
    }

    @GetMapping("/{ref}")
    public ApplicationDetailsResponse get(Actor actor,
            @PathVariable("ref") String referenceNumber) {
        return applicationService.get(referenceNumber, ownerScope(actor));
    }

    @PutMapping("/{ref}/correction")
    @Allow(SessionRole.OWNER)
    public ApplicationDetailsResponse correct(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody NewVehicleRegistrationRequest request) {
        return applicationService.correct(referenceNumber, request, actor.id());
    }

    @PostMapping("/{ref}/review")
    @Allow({SessionRole.RTO_OFFICER, SessionRole.RTO_ADMIN})
    public ApplicationDetailsResponse review(Actor actor,
            @PathVariable("ref") String referenceNumber,
            @Valid @RequestBody ApplicationReviewRequest request) {
        return applicationService.review(referenceNumber, request, actor.id());
    }
}
