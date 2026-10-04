package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationPageResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.ApplicationReviewService;
import com.nexturn.vehicleregistration.service.PaymentService;
import com.nexturn.vehicleregistration.service.RegistrationSubmissionService;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class VehicleRegistrationApplicationServiceImpl
        implements VehicleRegistrationApplicationService {

    private final RegistrationSubmissionService registrationSubmissionService;
    private final PaymentService paymentService;
    private final ApplicationReviewService applicationReviewService;
    private final ApplicationQueryService applicationQueryService;

    public VehicleRegistrationApplicationServiceImpl(
            RegistrationSubmissionService registrationSubmissionService,
            PaymentService paymentService,
            ApplicationReviewService applicationReviewService,
            ApplicationQueryService applicationQueryService) {

        this.registrationSubmissionService = registrationSubmissionService;
        this.paymentService = paymentService;
        this.applicationReviewService = applicationReviewService;
        this.applicationQueryService = applicationQueryService;
    }

    @Override
    public ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request,
            Actor actor) {

        return registrationSubmissionService.submit(request, actor);
    }

    @Override
    public ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request,
            Actor actor) {

        return registrationSubmissionService.correct(
                referenceNumber, request, actor);
    }

    @Override
    public ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request,
            Actor actor) {

        return paymentService.pay(referenceNumber, request, actor);
    }

    @Override
    public ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request,
            Actor actor) {

        return applicationReviewService.review(
                referenceNumber, request, actor);
    }

    @Override
    public ApplicationPageResponse list(
            Actor actor, int page, int size) {

        return applicationQueryService.list(actor, page, size);
    }

    @Override
    public ApplicationDetailsResponse get(
            String referenceNumber, Actor actor) {

        return applicationQueryService.get(referenceNumber, actor);
    }

    @Override
    public List<VehicleResponse> vehicles(Actor actor) {
        return applicationQueryService.vehicles(actor);
    }

    @Override
    public RegistrationCertificateResponse certificate(
            String referenceNumber, Actor actor) {

        return applicationQueryService.certificate(referenceNumber, actor);
    }
}