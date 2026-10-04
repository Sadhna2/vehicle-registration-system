package com.nexturn.vehicleregistration.auth;

import com.nexturn.vehicleregistration.enums.SessionRole;

public record Actor(Long id, SessionRole role, String name) implements java.io.Serializable {}
