package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.AccessDeniedException;
import com.nexturn.vehicleregistration.exception.InvalidTokenException;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authenticates every /api request (except {@link PublicEndpoint}s) from its Bearer token, checks
 * the account is still active with the same role, and enforces {@link Allow} role lists.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final JwtService jwt;
  private final OwnerRepository owners;
  private final RTOEmployeeRepository employees;

  public AuthInterceptor(
      JwtService jwt, OwnerRepository owners, RTOEmployeeRepository employees) {
    this.jwt = jwt;
    this.owners = owners;
    this.employees = employees;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod method)
        || method.hasMethodAnnotation(PublicEndpoint.class)) {
      return true;
    }
    Actor actor = jwt.verify(bearerToken(request)).actor();
    requireActive(actor);
    request.setAttribute(CurrentActor.ATTRIBUTE, actor);
    Allow allow = method.getMethodAnnotation(Allow.class);
    if (allow == null) {
      allow = method.getBeanType().getAnnotation(Allow.class);
    }
    if (allow != null && Arrays.stream(allow.value()).noneMatch(role -> role == actor.role())) {
      throw new AccessDeniedException("You do not have permission to perform this action");
    }
    return true;
  }

  public static String bearerToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
      throw new InvalidTokenException("Sign in to continue.");
    }
    return header.substring(7).trim();
  }

  private void requireActive(Actor actor) {
    boolean active =
        actor.role() == SessionRole.OWNER
            ? owners
                .findById(actor.id())
                .map(owner -> owner.getStatus() == AccountStatus.ACTIVE)
                .orElse(false)
            : employees
                .findById(actor.id())
                .map(
                    employee ->
                        employee.getStatus() == AccountStatus.ACTIVE
                            && SessionRole.of(employee.getRole()) == actor.role())
                .orElse(false);
    if (!active) {
      throw new InvalidTokenException("Your account is no longer active. Please sign in again.");
    }
  }
}
