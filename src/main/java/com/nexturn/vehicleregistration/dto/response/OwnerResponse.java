package com.nexturn.vehicleregistration.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.IdentityProofType;

public record OwnerResponse(
        Long ownerId,
        String firstName,
        String lastName,
        String emailAddress,
        String phoneNumber,
        LocalDate dateOfBirth,
        IdentityProofType identityProofType,
        String identityProofNumber,
        String address,
        String cityName,
        String stateName,
        String pincode,
        AccountStatus status,
        Instant dateOfCreation,
        Instant dateOfUpdate
) {
}
