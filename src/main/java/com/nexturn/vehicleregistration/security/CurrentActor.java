package com.nexturn.vehicleregistration.security;

import java.util.Optional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Reads the authenticated caller stored on the current HTTP request by AuthInterceptor. */
public final class CurrentActor {

  static final String ATTRIBUTE = CurrentActor.class.getName();

  private CurrentActor() {}

  public static Optional<Actor> get() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(
        (Actor) attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_REQUEST));
  }

  /** Audit label such as "RTO_OFFICER:3", or "SYSTEM" outside a request. */
  public static String label() {
    return get().map(Actor::label).orElse("SYSTEM");
  }
}
