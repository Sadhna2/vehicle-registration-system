package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.BulkAccountRequest;
import com.nexturn.vehicleregistration.dto.request.BulkIdsRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReferencesRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReviewRequest;
import com.nexturn.vehicleregistration.dto.response.BulkResultResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.BulkOperationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bulk actions, bulk updates and bulk deletes. Each returns a per-item result list. */
@RestController
@RequestMapping("/api")
public class BulkOperationController {
    @Autowired
    private BulkOperationService bulkService;

    /** One review action (START, VERIFY, SCHEDULE or REJECT) on many applications. */
    @PostMapping("/applications/bulk/review")
    @Allow({SessionRole.RTO_OFFICER, SessionRole.RTO_ADMIN})
    public BulkResultResponse review(Actor actor, @Valid @RequestBody BulkReviewRequest request) {
        return bulkService.review(request, actor.id());
    }

    /** The owner withdraws (deletes) their own unpaid applications that are not yet under review. */
    @PostMapping("/applications/bulk/withdraw")
    @Allow(SessionRole.OWNER)
    public BulkResultResponse withdraw(Actor actor, @Valid @RequestBody BulkReferencesRequest request) {
        return bulkService.withdrawApplications(request, actor.id());
    }

    /** Sets the status (and optionally the employee role) of many owner or employee accounts. */
    @PatchMapping("/accounts/{kind}/bulk")
    @Allow(SessionRole.SYSTEM_ADMIN)
    public BulkResultResponse accounts(Actor actor, @PathVariable("kind") String accountType,
            @Valid @RequestBody BulkAccountRequest request) {
        return bulkService.updateAccounts(accountType, request, actor.id());
    }

    /** Deletes fee rules that no application uses. */
    @PostMapping("/fees/bulk/delete")
    @Allow(SessionRole.RTO_ADMIN)
    public BulkResultResponse deleteFees(@Valid @RequestBody BulkIdsRequest request) {
        return bulkService.deleteFeeRules(request);
    }
}
