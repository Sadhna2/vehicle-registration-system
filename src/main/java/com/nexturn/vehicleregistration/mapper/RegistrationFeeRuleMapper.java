package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.RegistrationFeeRuleResponse;
import com.nexturn.vehicleregistration.entity.RegistrationFeeRule;

public final class RegistrationFeeRuleMapper {
  private RegistrationFeeRuleMapper() {}

  public static RegistrationFeeRuleResponse toResponse(RegistrationFeeRule f) {
    return new RegistrationFeeRuleResponse(
        f.getFeeRuleId(),
        f.getVehicleCategory(),
        f.getApplicationType(),
        f.getFeeAmount(),
        f.getEffectiveFrom());
  }
}
