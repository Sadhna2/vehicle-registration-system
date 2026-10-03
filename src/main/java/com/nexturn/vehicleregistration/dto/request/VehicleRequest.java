package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.FuelType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(

        @NotNull
        Long ownerId,
        @NotNull
        VehicleCategory vehicleCategory,
        @NotBlank
        @Size(max = 50)
        String manufacturerName
) {
}