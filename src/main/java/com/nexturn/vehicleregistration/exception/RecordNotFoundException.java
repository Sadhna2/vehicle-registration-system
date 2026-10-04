package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class RecordNotFoundException extends RuntimeException implements ApiError {
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

