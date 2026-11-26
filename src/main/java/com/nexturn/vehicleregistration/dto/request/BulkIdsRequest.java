package com.nexturn.vehicleregistration.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A list of record ids (fee rule ids) for a bulk delete. */
public record BulkIdsRequest(@NotEmpty @Size(max = 200) List<@NotNull Long> ids) {
}
