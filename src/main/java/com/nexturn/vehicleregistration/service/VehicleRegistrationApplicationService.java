package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
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
            NewVehicleRegistrationRequest request,
            Actor actor);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request,
            Actor actor);

    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request,
            Actor actor);

    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request,
            Actor actor);

    ApplicationPageResponse list(
            Actor actor, int page, int size);

    ApplicationDetailsResponse get(
            String referenceNumber, Actor actor);

    List<VehicleResponse> vehicles(Actor actor);

    RegistrationCertificateResponse certificate(
            String referenceNumber, Actor actor);
}