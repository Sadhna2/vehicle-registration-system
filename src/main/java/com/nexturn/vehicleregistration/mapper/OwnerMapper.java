package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.entity.Owner;

public final class OwnerMapper {

    private OwnerMapper() {
    }

    public static OwnerResponse toResponse(Owner owner) {

        if (owner == null) {
            return null;
        }

        return new OwnerResponse(
                owner.getOwnerId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getEmailAddress(),
                owner.getPhoneNumber(),
                owner.getDateOfBirth(),
                owner.getIdentityProofType(),
                owner.getIdentityProofNumber(),
                owner.getAddress(),
                owner.getCityName(),
                owner.getStateName(),
                owner.getPincode(),
                owner.getStatus(),
                owner.getDateOfCreation(),
                owner.getDateOfUpdate()
        );
    }
}
