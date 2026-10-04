package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

public interface RegistrationSubmissionService {

    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request,
            Actor actor);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request,
            Actor actor);
}