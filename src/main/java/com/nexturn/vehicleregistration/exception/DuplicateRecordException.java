package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRecordException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
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
