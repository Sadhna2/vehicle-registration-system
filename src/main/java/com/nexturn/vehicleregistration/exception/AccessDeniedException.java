package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class AccessDeniedException extends RuntimeException implements ApiError {
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
