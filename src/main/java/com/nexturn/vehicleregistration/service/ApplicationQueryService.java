package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

import java.util.List;

public interface ApplicationQueryService {
    ApplicationDetailsResponse detail(
            VehicleRegistrationApplication application);

    List<ApplicationSummaryResponse> list(Long ownerId);

    /** {@code ownerId} is the calling owner's id, or null for staff (no ownership check). */
    ApplicationDetailsResponse get(
            String referenceNumber, Long ownerId);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber, Long ownerId);
}
