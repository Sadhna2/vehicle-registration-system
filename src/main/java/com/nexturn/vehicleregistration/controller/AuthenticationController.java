package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.auth.ApiRequest;
import com.nexturn.vehicleregistration.auth.JwtService;
import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.dto.response.AuthenticationResponse;
import com.nexturn.vehicleregistration.service.AuthenticationService;

import jakarta.validation.Valid;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final String TOKEN_COOKIE_NAME = "VRS_TOKEN";
    private static final String COOKIE_PATH = "/api";

    private final AuthenticationService authenticationService;
    private final Access access;
    private final JwtService jwtService;
    private final boolean secureCookie;

    public AuthenticationController(
            AuthenticationService authenticationService,
            Access access,
            JwtService jwtService,
            @Value("${vrs.jwt.cookie-secure:false}") boolean secureCookie) {

        this.authenticationService = authenticationService;
        this.access = access;
        this.jwtService = jwtService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signup(
            @ApiRequest RequestInfo requestInfo,
            @Valid @RequestBody OwnerRequest request) {

        Actor actor = authenticationService.signup(request);

        return authenticate(actor);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @ApiRequest RequestInfo requestInfo,
            @Valid @RequestBody LoginRequest request) {

        Actor actor = authenticationService.login(request);

        return authenticate(actor);
    }

    @GetMapping("/me")
    public Actor me(@ApiRequest RequestInfo requestInfo) {
        return access.actor(requestInfo);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @ApiRequest RequestInfo requestInfo) {

        String token = access.token(requestInfo);

        if (token != null) {
            jwtService.revoke(token);
        }

        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    private ResponseEntity<AuthenticationResponse> authenticate(Actor actor) {

        JwtService.IssuedToken token = jwtService.issue(actor);

        Duration cookieLifetime =
                Duration.between(Instant.now(), token.expiresAt());

        ResponseCookie cookie = ResponseCookie
                .from(TOKEN_COOKIE_NAME, token.value())
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(cookieLifetime)
                .build();

        AuthenticationResponse response = new AuthenticationResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                actor);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(response);
    }
}