package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationPageResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

import java.util.List;

public interface ApplicationQueryService {

    ApplicationDetailsResponse detail(
            VehicleRegistrationApplication application);

    ApplicationPageResponse list(Actor actor, int page, int size);

    ApplicationDetailsResponse get(
            String referenceNumber, Actor actor);

    List<VehicleResponse> vehicles(Actor actor);

    RegistrationCertificateResponse certificate(
            String referenceNumber, Actor actor);
}