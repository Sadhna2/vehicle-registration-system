package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class JwtConfigurationException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public JwtConfigurationException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public String getCode() {
    return "JWT_CONFIGURATION_ERROR";
  }
}
