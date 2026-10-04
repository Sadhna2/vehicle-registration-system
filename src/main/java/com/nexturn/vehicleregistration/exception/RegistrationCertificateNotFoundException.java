package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class RegistrationCertificateNotFoundException extends RuntimeException implements ApiError {
  public RegistrationCertificateNotFoundException(String registrationNumber) {
    super("Registration certificate not found: " + registrationNumber);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "CERTIFICATE_NOT_FOUND";
  }
}

