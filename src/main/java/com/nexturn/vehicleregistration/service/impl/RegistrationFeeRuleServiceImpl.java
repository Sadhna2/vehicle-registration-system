package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;

import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.mapper.RegistrationFeeRuleMapper;
import com.nexturn.vehicleregistration.repository.RegistrationFeeRuleRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.RegistrationFeeRuleService;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationFeeRuleServiceImpl
        implements RegistrationFeeRuleService {

    private final RegistrationFeeRuleRepository feeRuleRepository;
    private final AuditService auditService;

    public RegistrationFeeRuleServiceImpl(
            RegistrationFeeRuleRepository feeRuleRepository,
            AuditService auditService) {

        this.feeRuleRepository = feeRuleRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationFeeRuleResponse> fees() {

        return feeRuleRepository
                .findAll(Sort.by("effectiveFrom").descending())
                .stream()
                .filter(rule -> rule.getApplicationType() == ApplicationType.NEW)
                .map(RegistrationFeeRuleMapper::toResponse)
                .toList();
    }

    @Override
    public RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request) {

        ensure(
                request.applicationType() == ApplicationType.NEW,
                "Only new vehicle registration fee rules are supported");

        RegistrationFeeRule feeRule = new RegistrationFeeRule();

        feeRule.setVehicleCategory(request.vehicleCategory());
        feeRule.setApplicationType(ApplicationType.NEW);
        feeRule.setFeeAmount(request.feeAmount());
        feeRule.setEffectiveFrom(request.effectiveFrom());

        RegistrationFeeRule savedFeeRule = feeRuleRepository.save(feeRule);

        auditService.record(
                null,
                "FEE_CREATED",
                ApplicationType.NEW.name());

        return RegistrationFeeRuleMapper.toResponse(savedFeeRule);
    }
}
