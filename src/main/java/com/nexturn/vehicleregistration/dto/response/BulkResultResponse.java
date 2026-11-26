package com.nexturn.vehicleregistration.dto.response;

import java.util.List;

/**
 * Outcome of a bulk operation. Each item is processed on its own, so some can succeed while
 * others fail; {@code results} says what happened to every item.
 */
public record BulkResultResponse(
        int requested, int succeeded, int failed, List<Item> results) {

    /** One item: its id, whether it worked, its new status, or the error code and message. */
    public record Item(String id, boolean success, String status, String code, String message) {}

    public static BulkResultResponse of(List<Item> results) {
        int succeeded = (int) results.stream().filter(Item::success).count();
        return new BulkResultResponse(results.size(), succeeded, results.size() - succeeded, results);
    }
}
