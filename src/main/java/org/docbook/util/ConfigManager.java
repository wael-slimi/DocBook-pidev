package org.docbook.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager - Centralized configuration management
 * Loads application settings from config.properties file
 * Prevents hardcoding sensitive information like API keys
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager() {
        loadProperties();
    }

    /**
     * Singleton instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load properties from config.properties file
     */
    private void loadProperties() {
        properties = new Properties();
        try {
            // Load from classpath resources
            InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                System.err.println("❌ config.properties file not found!");
                throw new FileNotFoundException("config.properties not found in resources");
            }

            properties.load(input);
            input.close();

            System.out.println("✓ Configuration loaded successfully");
            validateRequiredProperties();

        } catch (IOException e) {
            System.err.println("❌ Error loading config.properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validate that required properties are set
     */
    private void validateRequiredProperties() {
        String[] required = {
                "gemini.api.key",
                "gemini.api.url",
                "gemini.model",
                "ai.system.instruction"
        };

        for (String key : required) {
            if (getProperty(key).equals("YOUR_GEMINI_API_KEY_HERE") ||
                    getProperty(key).isEmpty()) {
                System.err.println("⚠️  WARNING: " + key + " is not configured!");
            }
        }
    }

    /**
     * Get property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }

    /**
     * Get property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get integer property
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean property
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    /**
     * Print all loaded properties (for debugging)
     */
    public void printAllProperties() {
        System.out.println("\n=== Loaded Configuration ===");
        properties.forEach((key, value) -> {
            // Mask sensitive values
            String displayValue = key.toString().contains("key")
                    ? "***" + value.toString().substring(Math.max(0, value.toString().length() - 4))
                    : value.toString();
            System.out.println(key + " = " + displayValue);
        });
        System.out.println("============================\n");
    }
}
