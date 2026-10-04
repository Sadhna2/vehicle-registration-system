package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends RuntimeException implements ApiError {
  public TokenExpiredException() {
    super("Authentication token expired. Please sign in again.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }

  public String getCode() {
    return "TOKEN_EXPIRED";
  }
}