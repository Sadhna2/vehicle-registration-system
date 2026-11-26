package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.BulkAccountRequest;
import com.nexturn.vehicleregistration.dto.request.BulkIdsRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReferencesRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReviewRequest;
import com.nexturn.vehicleregistration.dto.response.BulkResultResponse;
import com.nexturn.vehicleregistration.exception.ApiError;
import com.nexturn.vehicleregistration.service.ApplicationReviewService;
import com.nexturn.vehicleregistration.service.BulkOperationService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;
import com.nexturn.vehicleregistration.service.RegistrationSubmissionService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Deliberately not @Transactional: each item goes through a transactional single-record service,
 * so it commits or rolls back on its own.
 */
@Service
public class BulkOperationServiceImpl implements BulkOperationService {

    private static final Logger LOG = LoggerFactory.getLogger(BulkOperationServiceImpl.class);

    @Autowired
    private ApplicationReviewService reviewService;

    @Autowired
    private RTOEmployeeService employeeService;

    @Autowired
    private RegistrationFeeRuleService feeRuleService;

    @Autowired
    private RegistrationSubmissionService submissionService;

    @Override
    public BulkResultResponse review(BulkReviewRequest request, Long employeeId) {
        ApplicationReviewRequest review =
                new ApplicationReviewRequest(request.action(), request.remarks(), request.appointment());
        return run(request.references(), reference ->
                reviewService.review(reference.trim(), review, employeeId).applicationStatus().name());
    }

    @Override
    public BulkResultResponse updateAccounts(
            String accountType, BulkAccountRequest request, Long actingEmployeeId) {
        AccountRequest update = new AccountRequest(request.status(), request.role());
        return run(request.ids(), id -> {
            employeeService.account(accountType, id, update, actingEmployeeId);
            return request.status().name();
        });
    }

    @Override
    public BulkResultResponse deleteFeeRules(BulkIdsRequest request) {
        return run(request.ids(), id -> {
            feeRuleService.delete(id);
            return "DELETED";
        });
    }

    @Override
    public BulkResultResponse withdrawApplications(BulkReferencesRequest request, Long ownerId) {
        return run(request.references(), reference -> {
            submissionService.withdraw(reference.trim(), ownerId);
            return "WITHDRAWN";
        });
    }

    /** Runs the action once per distinct id and records success or the reason for failure. */
    private <T> BulkResultResponse run(Collection<T> ids, Function<T, String> action) {
        List<BulkResultResponse.Item> results = new ArrayList<>();
        for (T id : new LinkedHashSet<>(ids)) {
            String key = String.valueOf(id);
            try {
                results.add(new BulkResultResponse.Item(key, true, action.apply(id), null, null));
            } catch (RuntimeException failure) {
                results.add(failed(key, failure));
            }
        }
        return BulkResultResponse.of(results);
    }

    private static BulkResultResponse.Item failed(String id, RuntimeException failure) {
        if (failure instanceof ApiError error) {
            return new BulkResultResponse.Item(id, false, null, error.getCode(), failure.getMessage());
        }
        if (failure instanceof ConcurrencyFailureException) {
            return new BulkResultResponse.Item(id, false, null, "CONCURRENT_UPDATE",
                    "Record changed concurrently. Refresh and retry.");
        }
        if (failure instanceof DataIntegrityViolationException) {
            return new BulkResultResponse.Item(id, false, null, "DUPLICATE_RECORD",
                    "A related record prevents this change.");
        }
        LOG.error("Bulk operation failed for {}", id, failure);
        return new BulkResultResponse.Item(id, false, null, "INTERNAL_ERROR",
                "This item could not be processed.");
    }
}
