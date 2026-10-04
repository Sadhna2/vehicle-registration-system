package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class JwtSigningException extends RuntimeException implements ApiError {
  public JwtSigningException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public String getCode() {
    return "JWT_SIGNING_FAILED";
  }
}

