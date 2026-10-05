package com.nexturn.vehicleregistration.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationReviewRequest(

        @NotBlank
        String action,
        @Size(max = 500)
        String remarks,
        LocalDate appointment

) {
}
