package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.security.Actor;

/** Checks credentials and returns who signed in; the controller turns that into a JWT. */
public interface AuthenticationService {
    Actor signup(OwnerRequest request);

    Actor login(LoginRequest request);
}
