package com.nexturn.vehicleregistration.dto.request;

/** Headers supplied by Spring MVC, never accepted from a JSON request body. */
public record RequestInfo(String authorizationHeader, String cookieHeader) {}

