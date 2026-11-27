package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;

import com.nexturn.vehicleregistration.enums.SessionRole;
import java.util.List;

public interface RTOEmployeeService {
    List<RTOEmployeeResponse> employees();

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request, SessionRole creatorRole);

    List<OwnerResponse> owners();

    void account(
            String accountType,
            Long accountId,
            AccountRequest request, Long actingEmployeeId);
}
