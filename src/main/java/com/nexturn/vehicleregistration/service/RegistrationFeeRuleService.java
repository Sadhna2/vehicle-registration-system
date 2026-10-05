package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.RegistrationFeeRuleRequest;
import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;

import java.util.List;

public interface RegistrationFeeRuleService {
    List<RegistrationFeeRuleResponse> fees();

    RegistrationFeeRuleResponse fee(
            RegistrationFeeRuleRequest request);
}
