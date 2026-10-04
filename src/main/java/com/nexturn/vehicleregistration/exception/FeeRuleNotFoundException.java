package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class FeeRuleNotFoundException extends RuntimeException implements ApiError {
  public FeeRuleNotFoundException() {
    super("No registration fee is configured for this vehicle category.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "FEE_RULE_NOT_FOUND";
  }
}
