package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class DocumentNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public DocumentNotFoundException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "DOCUMENT_NOT_FOUND";
  }
}
