package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;

public record RTOEmployeeResponse(
        Long employeeId,
        String firstName,
        String lastName,
        String emailAddress,
        String phoneNumber,
        RTOEmployeeDesignation designation,
        RTOEmployeeRole role,
        AccountStatus status

) {
}