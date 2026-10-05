package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface ApplicationReviewService {
    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId);
}
