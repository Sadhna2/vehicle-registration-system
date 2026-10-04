package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

public interface ApiError {
  HttpStatus getStatus();

  String getCode();
}
