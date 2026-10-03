package com.nexturn.vehicleregistration.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        BigDecimal amount,
        String lastFour,
        String reference,
        Instant paidAt

) {
}
