package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class InspectionNotPassedException extends RuntimeException implements ApiError {
  public InspectionNotPassedException() {
    super("A passed inspection is required before approval.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "INSPECTION_NOT_PASSED";
  }
}
