package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class DuplicateRecordException extends RuntimeException implements ApiError {
  public DuplicateRecordException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "DUPLICATE_RECORD";
  }
}

