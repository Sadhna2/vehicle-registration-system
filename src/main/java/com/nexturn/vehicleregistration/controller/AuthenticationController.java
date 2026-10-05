package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.dto.response.LoginResponse;
import com.nexturn.vehicleregistration.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
    @RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/signup")
    public LoginResponse signup(@Valid @RequestBody OwnerRequest request) {
        return authenticationService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}
