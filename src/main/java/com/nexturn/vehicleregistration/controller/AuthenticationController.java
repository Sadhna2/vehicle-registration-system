package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.dto.response.AuthenticationResponse;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.AuthInterceptor;
import com.nexturn.vehicleregistration.security.JwtService;
import com.nexturn.vehicleregistration.security.PublicEndpoint;
import com.nexturn.vehicleregistration.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    @PublicEndpoint
    public ResponseEntity<AuthenticationResponse> signup(@Valid @RequestBody OwnerRequest request) {
        return issue(authenticationService.signup(request));
    }

    @PostMapping("/login")
    @PublicEndpoint
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return issue(authenticationService.login(request));
    }

    /** Returns the signed-in user, so the frontend can confirm a stored token is still valid. */
    @GetMapping("/me")
    public Actor me(Actor actor) {
        return actor;
    }

    /** Revokes the current token; it cannot be used again even before it expires. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        jwtService.revoke(AuthInterceptor.bearerToken(request));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<AuthenticationResponse> issue(Actor actor) {
        JwtService.IssuedToken token = jwtService.issue(actor);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AuthenticationResponse(
                        token.value(), "Bearer", token.expiresAt(), actor));
    }
}
