package com.nexturn.vehicleregistration.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import com.nexturn.vehicleregistration.exception.PasswordHashException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
  public static String hash(String value) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt)
        + ":"
        + Base64.getEncoder().encodeToString(derive(value, salt));
  }

  private static byte[] derive(String value, byte[] salt) {
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
          .generateSecret(new PBEKeySpec(value.toCharArray(), salt, 210000, 256))
          .getEncoded();
    } catch (Exception e) {
      throw new PasswordHashException(e);
    }
  }

  public static boolean matches(String value, String stored) {
    try {
      String[] parts = stored.split(":");
      return MessageDigest.isEqual(
          derive(value, Base64.getDecoder().decode(parts[0])),
          Base64.getDecoder().decode(parts[1]));
    } catch (Exception e) {
      return false;
    }
  }
}
