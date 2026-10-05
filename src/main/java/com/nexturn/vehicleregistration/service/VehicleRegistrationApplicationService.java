package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationPageResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleRegistrationApplicationService {

    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request);

    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request);

    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId);

    ApplicationPageResponse list(Long ownerId, int page, int size);

    ApplicationDetailsResponse get(
            String referenceNumber);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber);
}
