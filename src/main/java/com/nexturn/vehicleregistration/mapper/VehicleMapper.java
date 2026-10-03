package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.Vehicle;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {

        if (vehicle == null) {
            return null;
        }

        return new VehicleResponse(
                vehicle.getTemporaryregisterNo(),
                OwnerMapper.toResponse(vehicle.getCurrentOwner()),
                vehicle.getVehicleCategory(),
                vehicle.getManufacturerName(),
                vehicle.getModelName(),
                vehicle.getChassisNumber(),
                vehicle.getEngineNumber(),
                vehicle.getFuelType(),
                vehicle.getManufactureYear(),
                vehicle.getRegistrationcertificateNumber(),
                vehicle.getFirstRegistrationDate(),
                vehicle.getRegistrationValidTill(),
                vehicle.getColorVariant()
        );
    }
}
