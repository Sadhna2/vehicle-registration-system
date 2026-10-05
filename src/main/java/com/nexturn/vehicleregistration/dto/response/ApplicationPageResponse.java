package com.nexturn.vehicleregistration.dto.response;

import java.util.List;

public record ApplicationPageResponse(
        List<ApplicationSummaryResponse> content,
        long totalElements,
        int totalPages,
        int page
) {
}
