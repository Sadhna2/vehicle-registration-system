package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidLoginException extends RuntimeException implements ApiError {
  public InvalidLoginException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  public String getCode() {
    return "INVALID_LOGIN";
  }
}

