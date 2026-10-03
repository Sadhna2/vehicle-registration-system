package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RTOEmployeeRequest(

		@NotBlank @Size(max = 30) String firstName,
		@NotBlank @Size(max = 30) String lastName,
		@NotBlank @Email String emailAddress,
		@NotBlank @Size(min = 10, max = 128) String password,
		@NotNull @Pattern(regexp = "[0-9+]{10,15}") String phoneNumber,
		@NotNull RTOEmployeeDesignation designation,
		@NotNull RTOEmployeeRole role) {
}