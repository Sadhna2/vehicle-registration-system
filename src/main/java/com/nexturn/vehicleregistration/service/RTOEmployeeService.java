package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;

import java.util.List;

public interface RTOEmployeeService {

    List<RTOEmployeeResponse> employees(Actor actor);

    RTOEmployeeResponse employee(
            RTOEmployeeRequest request, Actor actor);

    List<OwnerResponse> owners(Actor actor);

    void account(
            String accountType,
            Long accountId,
            AccountRequest request,
            Actor actor);
}
