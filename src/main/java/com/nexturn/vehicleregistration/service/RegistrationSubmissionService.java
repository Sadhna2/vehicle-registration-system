package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface RegistrationSubmissionService {
    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request);
}
