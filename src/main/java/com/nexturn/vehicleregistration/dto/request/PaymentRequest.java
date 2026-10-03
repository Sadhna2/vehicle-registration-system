package com.nexturn.vehicleregistration.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PaymentRequest(

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        @Pattern(regexp = "[0-9]{4}")
        String cardLastFourDigit

) {
}
