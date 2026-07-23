package com.vzap.trytons.util;

import com.vzap.trytons.exceptions.ValidationException;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public final class PasswordUtil {

    private static final int BCRYPT_WORK_FACTOR = 12;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    // Utility class: prevent instantiation.
    private PasswordUtil() {
    }

    public static String hashPassword(String rawPassword) {
        validatePasswordForHashing(rawPassword);
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    public static boolean verifyPassword(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || rawPassword.isBlank() || storedPasswordHash == null || storedPasswordHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, storedPasswordHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void validatePasswordForHashing(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException("Password must not be blank.");
        }

        int passwordByteLength = rawPassword.getBytes(StandardCharsets.UTF_8).length;

        if (passwordByteLength > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new ValidationException("Password must not exceed 72 bytes when encoded as UTF-8.");
        }
    }
}