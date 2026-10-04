package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface ApplicationReviewService {

    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request,
            Actor actor);
}