package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.service.AdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
    @RequestMapping("/api/fees")
public class RegistrationFeeRuleController {
    @Autowired
    private AdministrationService administrationService;

    @GetMapping
    public List<RegistrationFeeRuleResponse> fees() { return administrationService.fees(); }

    @PostMapping
    public RegistrationFeeRuleResponse fee(@Valid @RequestBody RegistrationFeeRuleRequest request) {
        return administrationService.fee(request);
    }
}
