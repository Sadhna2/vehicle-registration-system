package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.AdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fees")
public class RegistrationFeeRuleController {
    @Autowired
    private AdministrationService administrationService;

    /** Any signed-in user may read the fee schedule. */
    @GetMapping
    public List<RegistrationFeeRuleResponse> fees() { return administrationService.fees(); }

    @PostMapping
    @Allow(SessionRole.RTO_ADMIN)
    public RegistrationFeeRuleResponse fee(@Valid @RequestBody RegistrationFeeRuleRequest request) {
        return administrationService.fee(request);
    }
}
