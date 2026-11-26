package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public InvalidTokenException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  public String getCode() {
    return "INVALID_TOKEN";
  }
}
