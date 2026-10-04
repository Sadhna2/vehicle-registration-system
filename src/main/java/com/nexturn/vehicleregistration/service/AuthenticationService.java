package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;

public interface AuthenticationService {

    Actor signup(OwnerRequest request);

    Actor login(LoginRequest request);
}
