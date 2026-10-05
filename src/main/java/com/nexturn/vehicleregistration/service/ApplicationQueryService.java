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

    ApplicationDetailsResponse get(
            String referenceNumber);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber);
}
