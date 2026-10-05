package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class RecordNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public RecordNotFoundException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "RECORD_NOT_FOUND";
  }
}
