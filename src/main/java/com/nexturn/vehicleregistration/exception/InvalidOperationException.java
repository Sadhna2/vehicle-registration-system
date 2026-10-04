package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends RuntimeException implements ApiError {
  public InvalidOperationException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "INVALID_OPERATION";
  }
}

