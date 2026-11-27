package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface RegistrationSubmissionService {
    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request, Long ownerId);

    /**
     * The owner deletes their own application while it is unpaid and not yet under review
     * (submitted or awaiting correction), together with its vehicle record and documents.
     */
    void withdraw(String referenceNumber, Long ownerId);
}
