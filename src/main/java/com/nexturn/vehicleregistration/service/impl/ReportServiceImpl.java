package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.PaymentStatus;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.mapper.AuditLogMapper;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.AuditLogRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.VechileRepository;
import com.nexturn.vehicleregistration.service.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final OwnerRepository ownerRepository;
    private final VechileRepository vehicleRepository;
    private final ApplicationWorkflowRepository applicationRepository;
    private final OwnerPaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final Access access;

    public ReportServiceImpl(
            OwnerRepository ownerRepository,
            VechileRepository vehicleRepository,
            ApplicationWorkflowRepository applicationRepository,
            OwnerPaymentRepository paymentRepository,
            AuditLogRepository auditLogRepository,
            Access access) {

        this.ownerRepository = ownerRepository;
        this.vehicleRepository = vehicleRepository;
        this.applicationRepository = applicationRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogRepository = auditLogRepository;
        this.access = access;
    }

    @Override
    public Map<String, Object> report(Actor actor) {

        access.require(actor, SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN);

        Map<String, Long> statusCounts = new LinkedHashMap<>();

        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }

        applicationRepository.countStatuses().forEach(
                row -> statusCounts.put(
                        row.getStatus().name(), row.getTotal()));

        BigDecimal collectedAmount =
                paymentRepository.collected(PaymentStatus.SUCCESS);

        if (collectedAmount == null) {
            collectedAmount = BigDecimal.ZERO;
        }

        return Map.of(
                "counts", statusCounts,
                "feeCollected", collectedAmount,
                "owners", ownerRepository.count(),
                "vehicles", vehicleRepository.count(),
                "expiringWithin90Days",
                vehicleRepository.countByRegistrationValidTillLessThanEqual(
                        LocalDate.now().plusDays(90)));
    }

    @Override
    public List<AuditLogResponse> audits(Actor actor) {

        access.require(actor, SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN);

        return auditLogRepository
                .findAll(
                        PageRequest.of(
                                0,
                                100,
                                Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }
}