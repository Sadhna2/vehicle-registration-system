package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.LoginResponse;
import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;

public interface AuthenticationService {
    LoginResponse signup(OwnerRequest request);

    LoginResponse login(LoginRequest request);
}
