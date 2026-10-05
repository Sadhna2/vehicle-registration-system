package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class ApplicationIdNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public ApplicationIdNotFoundException(String applicationId) {
    super("Application ID not found: " + applicationId);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "APPLICATION_ID_NOT_FOUND";
  }
}
