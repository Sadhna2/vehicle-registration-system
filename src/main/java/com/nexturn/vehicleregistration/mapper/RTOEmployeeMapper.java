package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.entity.RTOEmployee;

public final class RTOEmployeeMapper {

    private RTOEmployeeMapper() {
    }

    public static RTOEmployeeResponse toResponse(RTOEmployee employee) {

        if (employee == null) {
            return null;
        }

        return new RTOEmployeeResponse(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmailAddress(),
                employee.getPhoneNumber(),
                employee.getDesignation(),
                employee.getRole(),
                employee.getStatus(),
                employee.getDateOfJoining()
        );
    }
}
