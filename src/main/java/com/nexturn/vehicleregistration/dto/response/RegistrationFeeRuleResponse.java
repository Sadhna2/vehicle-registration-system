package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrationFeeRuleResponse(
    Long feeRuleId,
    VehicleCategory vehicleCategory,
    ApplicationType applicationType,
    BigDecimal feeAmount,
    LocalDate effectiveFrom) {}

