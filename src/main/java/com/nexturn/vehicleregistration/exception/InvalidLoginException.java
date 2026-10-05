package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidLoginException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
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
