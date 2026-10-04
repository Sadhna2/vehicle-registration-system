package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.auth.Actor;
import java.time.Instant;

public record AuthenticationResponse(
    String accessToken, String tokenType, Instant expiresAt, Actor user) {}
