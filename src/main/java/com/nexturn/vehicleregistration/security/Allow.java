package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.enums.SessionRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Restricts a controller method (or every method of a controller) to the listed roles. */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Allow {
  SessionRole[] value();
}
