package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.AdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Allow({SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN})
public class AdministrationController {
    @Autowired
    private AdministrationService administrationService;

    @GetMapping("/reports")
    public Map<String,Object> report() { return administrationService.report(); }

    @GetMapping("/audit")
    public List<AuditLogResponse> audit() { return administrationService.audits(); }

    @GetMapping("/employees")
    public List<RTOEmployeeResponse> employees() { return administrationService.employees(); }

    /** RTO admins may create officers only; system admins may create any employee role. */
    @PostMapping("/employees")
    public RTOEmployeeResponse employee(Actor actor, @Valid @RequestBody RTOEmployeeRequest request) {
        return administrationService.employee(request, actor.role());
    }

    @GetMapping("/owners")
    @Allow(SessionRole.SYSTEM_ADMIN)
    public List<OwnerResponse> owners() { return administrationService.owners(); }

    @PatchMapping("/accounts/{kind}/{id}")
    @Allow(SessionRole.SYSTEM_ADMIN)
    public void account(Actor actor, @PathVariable("kind") String accountType,
            @PathVariable("id") Long accountId, @Valid @RequestBody AccountRequest request) {
        administrationService.account(accountType, accountId, request, actor.id());
    }
}
