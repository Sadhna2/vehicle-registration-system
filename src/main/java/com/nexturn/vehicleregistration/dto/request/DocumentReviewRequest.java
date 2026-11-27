package com.nexturn.vehicleregistration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Officer decision on one document: VERIFY, or REJECT with remarks explaining what to fix. */
public record DocumentReviewRequest(
        @NotBlank @Pattern(regexp = "VERIFY|REJECT") String action,
        @Size(max = 500) String remarks) {
}
