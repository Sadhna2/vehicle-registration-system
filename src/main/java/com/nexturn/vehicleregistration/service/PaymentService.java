package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface PaymentService {

    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request,
            Actor actor);
}