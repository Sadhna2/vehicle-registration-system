package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.exception.InvalidOperationException;
import com.nexturn.vehicleregistration.exception.RecordNotFoundException;

import java.util.Optional;

public final class WorkflowSupport {
    private WorkflowSupport() {
    }

    public static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new InvalidOperationException(message);
        }
    }

    public static <T> T found(Optional<T> value) {
        return value.orElseThrow(
                () -> new RecordNotFoundException("Record not found"));
    }
}
