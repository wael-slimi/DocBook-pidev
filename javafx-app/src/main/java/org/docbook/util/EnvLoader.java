package org.docbook.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> envMap = new HashMap<>();
    private static final String ENV_FILE_PATH = "src/.env"; // Path relative to the project root

    static {
        loadEnv();
    }

    private static void loadEnv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENV_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load .env file from " + ENV_FILE_PATH + ". " + e.getMessage());
            // Fallback to system environment variables or default values if needed
        }
    }

    public static String get(String key) {
        return envMap.get(key);
    }

    public static String get(String key, String defaultValue) {
        return envMap.getOrDefault(key, defaultValue);
    }
}
