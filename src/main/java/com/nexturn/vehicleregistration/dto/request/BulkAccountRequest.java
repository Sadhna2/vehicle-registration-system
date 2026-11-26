package com.nexturn.vehicleregistration.dto.request;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Sets the same status (and, for employees, optionally the same role) on many accounts. */
public record BulkAccountRequest(
        @NotEmpty @Size(max = 200) List<@NotNull Long> ids,
        @NotNull AccountStatus status,
        RTOEmployeeRole role) {
}
