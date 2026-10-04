package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.*;
import jakarta.validation.constraints.*;

public record NewVehicleRegistrationRequest(
    @NotNull VehicleCategory vehicleCategory,
    @NotBlank @Size(max = 50) String manufacturerName,
    @NotBlank @Size(max = 50) String modelName,
    @NotBlank @Size(max = 50) String chassisNumber,
    @NotBlank @Size(max = 50) String engineNumber,
    @NotNull FuelType fuelType,
    @Min(1900) int manufactureYear,
    @Size(max = 30) String colorVariant) {}
