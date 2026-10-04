package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationPageResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.RegistrationCertificate;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.AccessDeniedException;
import com.nexturn.vehicleregistration.exception.RegistrationCertificateNotFoundException;
import com.nexturn.vehicleregistration.mapper.ApplicationDetailsMapper;
import com.nexturn.vehicleregistration.mapper.ApplicationSummaryMapper;
import com.nexturn.vehicleregistration.mapper.RegistrationCertificateMapper;
import com.nexturn.vehicleregistration.mapper.VehicleMapper;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.repository.RegistrationCertificateRepository;
import com.nexturn.vehicleregistration.repository.VechileRepository;
import com.nexturn.vehicleregistration.service.ApplicationAccessService;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApplicationQueryServiceImpl implements ApplicationQueryService {

    private final VechileRepository vehicleRepository;
    private final ApplicationWorkflowRepository applicationRepository;
    private final OwnerPaymentRepository paymentRepository;
    private final RegistrationCertificateRepository certificateRepository;
    private final Access access;
    private final ApplicationAccessService applicationAccessService;

    public ApplicationQueryServiceImpl(
            VechileRepository vehicleRepository,
            ApplicationWorkflowRepository applicationRepository,
            OwnerPaymentRepository paymentRepository,
            RegistrationCertificateRepository certificateRepository,
            Access access,
            ApplicationAccessService applicationAccessService) {

        this.vehicleRepository = vehicleRepository;
        this.applicationRepository = applicationRepository;
        this.paymentRepository = paymentRepository;
        this.certificateRepository = certificateRepository;
        this.access = access;
        this.applicationAccessService = applicationAccessService;
    }

    @Override
    public ApplicationDetailsResponse detail(
            VehicleRegistrationApplication application) {

        String referenceNumber = application.getApplicationRefNo();

        return ApplicationDetailsMapper.toResponse(
                application,
                paymentRepository
                        .findByApplicationApplicationRefNo(referenceNumber)
                        .orElse(null),
                certificateRepository
                        .findByApplicationApplicationRefNo(referenceNumber)
                        .orElse(null));
    }

    @Override
    public ApplicationPageResponse list(Actor actor, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(100, size)),
                Sort.by("submittedDate").descending());

        Page<VehicleRegistrationApplication> applications =
                actor.role() == SessionRole.OWNER
                        ? applicationRepository.findByApplicantOwnerId(
                                actor.id(), pageable)
                        : applicationRepository.findAll(pageable);

        return new ApplicationPageResponse(
                applications.stream()
                        .map(ApplicationSummaryMapper::toSummary)
                        .toList(),
                applications.getTotalElements(),
                applications.getTotalPages(),
                applications.getNumber());
    }

    @Override
    public ApplicationDetailsResponse get(
            String referenceNumber, Actor actor) {

        VehicleRegistrationApplication application =
                applicationAccessService.readable(referenceNumber, actor);

        return detail(application);
    }

    @Override
    public List<VehicleResponse> vehicles(Actor actor) {

        access.require(actor, SessionRole.OWNER);

        return vehicleRepository
                .findByCurrentOwnerOwnerId(actor.id())
                .stream()
                .map(VehicleMapper::toResponse)
                .toList();
    }

    @Override
    public RegistrationCertificateResponse certificate(
            String referenceNumber, Actor actor) {

        VehicleRegistrationApplication application =
                applicationAccessService.readable(referenceNumber, actor);

        String registrationNumber = application.getVehicle()
                .getRegistrationcertificateNumber();

        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new RegistrationCertificateNotFoundException(registrationNumber);
        }

        RegistrationCertificate certificate = certificateRepository
                .findById(registrationNumber)
                .orElseThrow(
                        () -> new RegistrationCertificateNotFoundException(
                                registrationNumber));

        if (actor.role() == SessionRole.OWNER
                && !certificate.getRegisteredOwner()
                        .getOwnerId().equals(actor.id())) {

            throw new AccessDeniedException(
                    "This registration certificate belongs to another owner");
        }

        return RegistrationCertificateMapper.toResponse(certificate);
    }
}