package org.docbook.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Symfony default cost is 12
    private static final int logRounds = 12;

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(logRounds));
    }

    public static boolean verify(String password, String hashedPassword) {
        // This works with Symfony's $2y$ or $2a$ prefixes
        return BCrypt.checkpw(password, hashedPassword);
    }
}