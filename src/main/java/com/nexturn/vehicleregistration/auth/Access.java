 package com.nexturn.vehicleregistration.auth;

import com.nexturn.vehicleregistration.dto.request.RequestInfo;
import com.nexturn.vehicleregistration.enums.*;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.*;
import com.nexturn.vehicleregistration.repository.*;


import org.springframework.stereotype.Component;

@Component
public class Access {
  private final JwtService jwt;
  private final OwnerRepository owners;
  private final RTOEmployeeRepository employees;

  public Access(OwnerRepository owners, RTOEmployeeRepository employees, JwtService jwt) {
    this.jwt = jwt;
    this.owners = owners;
    this.employees = employees;
  }

  public Actor actor(RequestInfo request) {
    Actor a = jwt.verify(token(request)).actor();
    boolean active =
        a.role() == SessionRole.OWNER
            ? owners.findById(a.id()).map(o -> o.getStatus() == AccountStatus.ACTIVE).orElse(false)
            : employees
                .findById(a.id())
                .map(
                    e ->
                        e.getStatus() == AccountStatus.ACTIVE
                            && e.getRole().name().equals(a.role().name()))
                .orElse(false);
    if (!active) {
      throw new InvalidLoginException("Account unavailable; sign in again");
    }
    return a;
  }

  public String token(RequestInfo request) {
    String header = request.authorizationHeader();
    if (header != null) {
      if (!header.startsWith("Bearer ") || header.length() <= 7)
        throw new InvalidTokenException("Bearer token required");
      return header.substring(7);
    }
    String cookieHeader = request.cookieHeader();
    if (cookieHeader != null)
      for (String item : cookieHeader.split(";")) {
        String part = item.trim();
        if (part.startsWith("VRS_TOKEN=")) {
          String value = part.substring("VRS_TOKEN=".length());
          if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1)
            value = value.substring(1, value.length() - 1);
          return value;
        }
      }
    return null;
  }

  public void require(Actor a, SessionRole... roles) {
    if (java.util.Arrays.stream(roles).noneMatch(r -> r == a.role()))
      throw new AccessDeniedException("You do not have permission to perform this action");
  }
}
