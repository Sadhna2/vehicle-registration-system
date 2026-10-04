package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class ApplicationIdNotFoundException extends RuntimeException implements ApiError {
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
