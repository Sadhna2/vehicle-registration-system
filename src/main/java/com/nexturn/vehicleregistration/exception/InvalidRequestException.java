package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends RuntimeException implements ApiError {
  public InvalidRequestException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public String getCode() {
    return "INVALID_REQUEST";
  }
}

