package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.BulkAccountRequest;
import com.nexturn.vehicleregistration.dto.request.BulkIdsRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReferencesRequest;
import com.nexturn.vehicleregistration.dto.request.BulkReviewRequest;
import com.nexturn.vehicleregistration.dto.response.BulkResultResponse;

/**
 * Applies one operation to many records. Each record is processed in its own transaction through
 * the normal single-record service, so every workflow rule, permission check and audit entry still
 * applies, and one failure never undoes the others.
 */
public interface BulkOperationService {

    BulkResultResponse review(BulkReviewRequest request, Long employeeId);

    BulkResultResponse updateAccounts(String accountType, BulkAccountRequest request, Long actingEmployeeId);

    BulkResultResponse deleteFeeRules(BulkIdsRequest request);

    BulkResultResponse withdrawApplications(BulkReferencesRequest request, Long ownerId);
}
