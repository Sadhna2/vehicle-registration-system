package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class PaymentRequiredException extends RuntimeException implements ApiError {
  public PaymentRequiredException() {
    super("A successful payment is required before approval.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "PAYMENT_REQUIRED";
  }
}

