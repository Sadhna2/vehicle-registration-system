package com.nexturn.vehicleregistration.security;

import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.ApiError;
import com.nexturn.vehicleregistration.exception.InvalidTokenException;
import com.nexturn.vehicleregistration.exception.JwtConfigurationException;
import com.nexturn.vehicleregistration.exception.TokenExpiredException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * HS256 JSON Web Tokens: fixed algorithm, issuer/audience/expiry checks, constant-time signature
 * comparison, and in-memory revocation on logout.
 */
@Service
public class JwtService {

  private static final String ISSUER = "vehicle-registration-system";
  private static final String AUDIENCE = "vehicle-registration-portal";
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private final byte[] secret;
  private final Duration lifetime;
  private final Clock clock;
  private final JsonMapper json = JsonMapper.builder().build();
  private final Map<String, Long> revoked = new ConcurrentHashMap<>();

  @Autowired
  public JwtService(
      @Value("${vrs.jwt.secret:}") String secret,
      @Value("${vrs.jwt.lifetime-minutes:60}") int minutes) {
    this(secret, Duration.ofMinutes(minutes), Clock.systemUTC());
  }

  JwtService(String secret, Duration lifetime, Clock clock) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new JwtConfigurationException(
          "Set JWT_SECRET (vrs.jwt.secret) to at least 32 random characters");
    }
    if (lifetime.isNegative()
        || lifetime.isZero()
        || lifetime.compareTo(Duration.ofHours(24)) > 0) {
      throw new JwtConfigurationException("JWT lifetime must be between 1 minute and 24 hours");
    }
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.lifetime = lifetime;
    this.clock = clock;
  }

  public record IssuedToken(String value, Instant expiresAt) {}

  public record VerifiedToken(Actor actor, String id, long expiresAt) {}

  public IssuedToken issue(Actor actor) {
    long issued = clock.instant().getEpochSecond();
    long expires = issued + lifetime.toSeconds();
    String header = encode(json.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
    String payload =
        encode(
            json.writeValueAsBytes(
                Map.of(
                    "iss", ISSUER,
                    "aud", AUDIENCE,
                    "sub", actor.id().toString(),
                    "role", actor.role().name(),
                    "name", actor.name(),
                    "iat", issued,
                    "exp", expires,
                    "jti", UUID.randomUUID().toString())));
    String content = header + "." + payload;
    return new IssuedToken(content + "." + encode(sign(content)), Instant.ofEpochSecond(expires));
  }

  public VerifiedToken verify(String token) {
    try {
      if (token == null || token.length() > 8192) {
        throw unauthorized();
      }
      String[] parts = token.split("\\.", -1);
      if (parts.length != 3) {
        throw unauthorized();
      }
      for (String part : parts) {
        if (!part.matches("[A-Za-z0-9_-]+")) {
          throw unauthorized();
        }
      }
      if (!MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
        throw unauthorized();
      }
      JsonNode header = json.readTree(DECODER.decode(parts[0]));
      if (!"HS256".equals(header.path("alg").asString())
          || !"JWT".equals(header.path("typ").asString())
          || header.has("crit")) {
        throw unauthorized();
      }
      JsonNode claims = json.readTree(DECODER.decode(parts[1]));
      if (!ISSUER.equals(claims.path("iss").asString())
          || !AUDIENCE.equals(claims.path("aud").asString())
          || !claims.path("iat").isIntegralNumber()
          || !claims.path("exp").isIntegralNumber()) {
        throw unauthorized();
      }
      long now = clock.instant().getEpochSecond();
      long issued = claims.path("iat").asLong();
      long expires = claims.path("exp").asLong();
      String id = claims.path("jti").asString();
      revoked.entrySet().removeIf(entry -> entry.getValue() <= now);
      if (expires <= now) {
        throw new TokenExpiredException("Your session has expired. Please sign in again.");
      }
      if (issued > now + 60
          || expires <= issued
          || expires - issued > lifetime.toSeconds()
          || !id.matches("[0-9a-f-]{36}")
          || revoked.containsKey(id)) {
        throw unauthorized();
      }
      long userId = Long.parseLong(claims.path("sub").asString());
      if (userId <= 0) {
        throw unauthorized();
      }
      SessionRole role = SessionRole.valueOf(claims.path("role").asString());
      return new VerifiedToken(
          new Actor(userId, role, claims.path("name").asString()), id, expires);
    } catch (RuntimeException invalid) {
      if (invalid instanceof ApiError) {
        throw invalid;
      }
      throw unauthorized();
    }
  }

  /** Revokes a token until it would have expired anyway (used by logout). */
  public void revoke(String token) {
    VerifiedToken verified = verify(token);
    revoked.put(verified.id(), verified.expiresAt());
  }

  private byte[] sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
    } catch (GeneralSecurityException failure) {
      throw new JwtConfigurationException("JWT signing is unavailable");
    }
  }

  private static String encode(byte[] bytes) {
    return ENCODER.encodeToString(bytes);
  }

  private static InvalidTokenException unauthorized() {
    return new InvalidTokenException("Invalid authentication token. Please sign in again.");
  }
}
