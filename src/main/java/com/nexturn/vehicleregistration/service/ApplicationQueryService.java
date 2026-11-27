package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;

import java.util.List;

public interface ApplicationQueryService {
    ApplicationDetailsResponse detail(
            VehicleRegistrationApplication application);

    /** Applications, newest first. {@code ownerId} null = all owners; {@code status} null = any status. */
    List<ApplicationSummaryResponse> list(Long ownerId, ApplicationStatus status);

    /** {@code ownerId} is the calling owner's id, or null for staff (no ownership check). */
    ApplicationDetailsResponse get(
            String referenceNumber, Long ownerId);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber, Long ownerId);
}
