package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.entity.AuditLogs;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.repository.AuditLogRepository;
import com.nexturn.vehicleregistration.service.AuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(
            Actor actor,
            VehicleRegistrationApplication application,
            String action,
            String remarks) {

        AuditLogs auditLog = new AuditLogs();

        auditLog.setActor(actor.role() + ":" + actor.id());

        auditLog.setApplicationRefNo(
                application == null
                        ? null
                        : application.getApplicationRefNo());

        auditLog.setAction(action);
        auditLog.setRemarks(remarks);

        auditLogRepository.save(auditLog);
    }
}
