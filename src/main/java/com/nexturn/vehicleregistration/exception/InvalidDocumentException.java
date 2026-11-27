package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidDocumentException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public InvalidDocumentException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public String getCode() {
    return "INVALID_DOCUMENT";
  }
}
