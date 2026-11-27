package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public class DocumentStorageException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;

  public DocumentStorageException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public String getCode() {
    return "DOCUMENT_STORAGE_FAILED";
  }
}
