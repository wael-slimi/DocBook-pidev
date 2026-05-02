package org.docbook.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int logRounds = 12;

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    public static boolean verify(String password, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        // Normalize Symfony's $2y$ prefix to $2a$ for jBCrypt compatibility
        String normalizedHash = hashedPassword.replace("$2y$", "$2a$");
        try {
            return BCrypt.checkpw(password, normalizedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("BCrypt verify error: " + e.getMessage());
            return false;
        }
    }
}