package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class PasswordHashException extends RuntimeException implements ApiError {
  public PasswordHashException(Throwable cause) {
    super("Password hashing is unavailable.", cause);
  }

  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public String getCode() {
    return "PASSWORD_HASH_FAILED";
  }
}

