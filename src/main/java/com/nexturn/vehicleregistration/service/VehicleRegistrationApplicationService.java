package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;

import java.util.List;

/**
 * Facade used by the controllers. Every {@code ownerId} comes from the caller's verified token:
 * the owner's own id, or {@code null} when an RTO employee is calling (no owner restriction).
 */
public interface VehicleRegistrationApplicationService {
    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request, Long ownerId);

    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId);

    List<ApplicationSummaryResponse> list(Long ownerId, ApplicationStatus status);

    ApplicationDetailsResponse get(
            String referenceNumber, Long ownerId);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber, Long ownerId);
}
