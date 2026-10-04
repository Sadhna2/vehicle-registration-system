package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class CorsConfigurationException extends RuntimeException implements ApiError {

    public CorsConfigurationException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getCode() {
        return "CORS_CONFIGURATION_ERROR";
    }
}
