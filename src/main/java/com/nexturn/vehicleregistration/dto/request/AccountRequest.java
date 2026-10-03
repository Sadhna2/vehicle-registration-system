package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;

import jakarta.validation.constraints.NotNull;

public record AccountRequest(
		@NotNull AccountStatus status,
		RTOEmployeeRole role) {
}