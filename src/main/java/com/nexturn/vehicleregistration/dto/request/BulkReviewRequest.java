package com.nexturn.vehicleregistration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * The same review action applied to many applications. Only actions that make sense in bulk are
 * allowed; inspection results and approval stay one application at a time.
 */
public record BulkReviewRequest(
        @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 30) String> references,
        @NotBlank @Pattern(regexp = "START|VERIFY|SCHEDULE|REJECT") String action,
        @Size(max = 500) String remarks,
        LocalDate appointment) {
}
