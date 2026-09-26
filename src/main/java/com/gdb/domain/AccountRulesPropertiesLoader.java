package com.gdb.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AccountRulesPropertiesLoader {

    private final Properties properties = new Properties();

    public AccountRulesPropertiesLoader(String location) {
        String resourceName = normalizeResourceName(location);
        ClassLoader classLoader = AccountRulesPropertiesLoader.class.getClassLoader();
        InputStream resourceStream = classLoader.getResourceAsStream(resourceName);

        try {
            if (resourceStream != null) {
                try (InputStream stream = resourceStream) {
                    properties.load(stream);
                }
            } else {
                Path path = Paths.get(location);
                if (!Files.isRegularFile(path)) {
                    path = Paths.get("src", "main", "resources").resolve(resourceName);
                }
                try (InputStream stream = Files.newInputStream(path)) {
                    properties.load(stream);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load account rules: " + location, exception);
        }

        System.out.println("[Config] Loaded rules from src/main/resources/" + resourceName);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        String value = getProperty(key, Double.toString(defaultValue));
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid decimal rule for '" + key + "': " + value, exception);
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = getProperty(key, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid integer rule for '" + key + "': " + value, exception);
        }
    }

    public Double getAdditionalFeature(String featureKey, String tenureBucket) {
        String propertyPrefix = switch (featureKey) {
            case "dailyTransferLimit" -> "daily.transfer.limit";
            default -> featureKey;
        };
        String value = properties.getProperty(propertyPrefix + "." + tenureBucket);
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid decimal rule for '" + propertyPrefix
                    + "." + tenureBucket + "': " + value, exception);
        }
    }

    private static String normalizeResourceName(String location) {
        String normalized = location.replace('\\', '/');
        int configIndex = normalized.indexOf("config/rules/");
        if (configIndex >= 0) {
            return normalized.substring(configIndex);
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}