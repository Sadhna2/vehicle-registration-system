package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class EmployeeNotFoundException extends RuntimeException implements ApiError {
  public EmployeeNotFoundException(Long employeeId) {
    super("Employee not found: " + employeeId);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "EMPLOYEE_NOT_FOUND";
  }
}
