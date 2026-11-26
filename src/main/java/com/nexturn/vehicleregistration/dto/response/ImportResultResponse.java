package com.nexturn.vehicleregistration.dto.response;

import java.util.List;

/**
 * Result of a CSV import. A dry run only checks the file. A real import saves every row in one
 * transaction, and only when no row has an error ({@code imported} is 0 otherwise).
 */
public record ImportResultResponse(
        boolean dryRun, int totalRows, int validRows, int imported, List<RowError> errors) {

    /** {@code row} is the line number in the file (the header is line 1). */
    public record RowError(int row, String field, String message) {}
}
