package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public AccessDeniedException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.FORBIDDEN;
  }

  public String getCode() {
    return "ACCESS_DENIED";
  }
}
