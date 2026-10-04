package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.AccessDeniedException;
import com.nexturn.vehicleregistration.exception.ReferenceNumberNotFoundException;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.service.ApplicationAccessService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApplicationAccessServiceImpl implements ApplicationAccessService {

    private final ApplicationWorkflowRepository applicationRepository;
    private final Access access;

    public ApplicationAccessServiceImpl(
            ApplicationWorkflowRepository applicationRepository,
            Access access) {

        this.applicationRepository = applicationRepository;
        this.access = access;
    }

    @Override
    public VehicleRegistrationApplication readable(
            String referenceNumber, Actor actor) {

        VehicleRegistrationApplication application = applicationRepository
                .findById(referenceNumber)
                .orElseThrow(
                        () -> new ReferenceNumberNotFoundException(referenceNumber));

        ensure(
                application.getApplicationType() == ApplicationType.NEW,
                "Only new vehicle registration is supported");

        if (actor.role() == SessionRole.OWNER
                && !application.getApplicant().getOwnerId().equals(actor.id())) {

            throw new AccessDeniedException(
                    "This application belongs to another owner");
        }

        return application;
    }

    @Override
    public void ownerApplication(
            VehicleRegistrationApplication application, Actor actor) {

        access.require(actor, SessionRole.OWNER);

        ensure(
                application.getApplicationType() == ApplicationType.NEW,
                "Only new vehicle registration is supported");

        if (!application.getApplicant().getOwnerId().equals(actor.id())) {
            throw new AccessDeniedException(
                    "This application belongs to another owner");
        }
    }
}