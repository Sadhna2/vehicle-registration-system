package com.nexturn.vehicleregistration.dto.response;

import java.time.LocalDate;

public record RegistrationCertificateResponse(
        String registrationNumber,
        String applicationRefNo,
        Long issuedByEmployeeId,
        String issuedByEmployeeName,
        LocalDate issuedDate,
        LocalDate validTill,
        Long registeredOwnerId,
        String registeredOwnerName
) {
}