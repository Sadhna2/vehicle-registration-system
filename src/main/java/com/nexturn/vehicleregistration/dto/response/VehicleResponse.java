package com.nexturn.vehicleregistration.dto.response;

import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.FuelType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

public record VehicleResponse(
        Long temporaryRegisterNo,
        OwnerResponse currentOwner,
        VehicleCategory vehicleCategory,
        String manufacturerName,
        String modelName,
        String chassisNumber,
        String engineNumber,
        FuelType fuelType,
        int manufactureYear,
        String registrationCertificateNumber,
        LocalDate firstRegistrationDate,
        LocalDate registrationValidTill,
        String colorVariant

) {
}
