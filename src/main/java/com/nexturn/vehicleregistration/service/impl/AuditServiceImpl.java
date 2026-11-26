package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.entity.AuditLogs;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.repository.AuditLogRepository;
import com.nexturn.vehicleregistration.security.CurrentActor;
import com.nexturn.vehicleregistration.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void record(
            VehicleRegistrationApplication application,
            String action,
            String remarks) {
        AuditLogs auditLog = new AuditLogs();

        // The signed-in caller from the JWT, e.g. "RTO_OFFICER:3" or "OWNER:12".
        auditLog.setActor(CurrentActor.label());

        auditLog.setApplicationRefNo(
                application == null
                        ? null
                        : application.getApplicationRefNo());

        auditLog.setAction(action);
        auditLog.setRemarks(remarks);

        auditLogRepository.save(auditLog);
    }
}
