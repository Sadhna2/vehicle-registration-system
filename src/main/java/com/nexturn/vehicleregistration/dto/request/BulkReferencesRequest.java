package com.nexturn.vehicleregistration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A list of application reference numbers (for withdrawing applications). */
public record BulkReferencesRequest(
        @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 30) String> references) {
}
