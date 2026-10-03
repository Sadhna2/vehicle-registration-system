package com.nexturn.vehicleregistration.dto.response;

import java.time.LocalDate;

public record RegistrationCertificateResponse(
        String registrationCertificateNumber,
        Long vehicleId,
        Long ownerId,
        LocalDate issueDate,
        LocalDate validTill
) {
}
