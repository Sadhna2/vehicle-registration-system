package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.ApplicationReviewService;
import com.nexturn.vehicleregistration.service.PaymentService;
import com.nexturn.vehicleregistration.service.RegistrationSubmissionService;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleRegistrationApplicationServiceImpl
        implements VehicleRegistrationApplicationService {
    @Autowired
    private RegistrationSubmissionService registrationSubmissionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ApplicationReviewService applicationReviewService;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Override
    public ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId) {
        return registrationSubmissionService.submit(request, ownerId);
    }

    @Override
    public ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request, Long ownerId) {
        return registrationSubmissionService.correct(
                referenceNumber, request, ownerId);
    }

    @Override
    public ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request, Long ownerId) {
        return paymentService.pay(referenceNumber, request, ownerId);
    }

    @Override
    public ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId) {
        return applicationReviewService.review(
                referenceNumber, request, employeeId);
    }

    @Override
    public List<ApplicationSummaryResponse> list(Long ownerId, ApplicationStatus status) {
        return applicationQueryService.list(ownerId, status);
    }

    @Override
    public ApplicationDetailsResponse get(
            String referenceNumber, Long ownerId) {
        return applicationQueryService.get(referenceNumber, ownerId);
    }

    @Override
    public List<VehicleResponse> vehicles(Long ownerId) {
        return applicationQueryService.vehicles(ownerId);
    }

    @Override
    public RegistrationCertificateResponse certificate(
            String referenceNumber, Long ownerId) {
        return applicationQueryService.certificate(referenceNumber, ownerId);
    }
}
