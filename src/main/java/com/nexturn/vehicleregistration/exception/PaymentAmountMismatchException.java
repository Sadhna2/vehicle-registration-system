package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class PaymentAmountMismatchException extends RuntimeException implements ApiError {
  public PaymentAmountMismatchException() {
    super("Payment amount must equal the quoted registration fee.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public String getCode() {
    return "PAYMENT_AMOUNT_MISMATCH";
  }
}
