package com.vzap.trytons.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;

public final class DotEnvConfig {

    private static final Dotenv DOTENV = loadDotenv();

    private DotEnvConfig() {
    }

    public static String getRequired(String key) {
        String value = firstNonBlank(System.getProperty(key), System.getenv(key), DOTENV.get(key));

        if (value == null) {
            throw new IllegalStateException("Missing required configuration value: " + key + ". Set it as a GlassFish JVM -D property, an environment variable, " + "or in the configured env file.");
        }
        return value;
    }

    public static long getRequiredLong(String key) {
        try {
            return Long.parseLong(getRequired(key));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " must contain a valid whole number.", e);
        }
    }

    public static int getRequiredInt(String key) {
        try {
            return Integer.parseInt(getRequired(key));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(key + " must contain a valid integer.", e);
        }
    }

    private static Dotenv loadDotenv() {
        String filename = firstNonBlank(System.getProperty("TRYTONS_ENV_FILE"), System.getenv("TRYTONS_ENV_FILE"), ".env");
        String directory = firstNonBlank(System.getProperty("TRYTONS_ENV_DIR"), System.getenv("TRYTONS_ENV_DIR"));

        DotenvBuilder builder = Dotenv.configure().filename(filename).ignoreIfMissing();

        if (directory != null) {
            builder.directory(directory);
        }
        return builder.load();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
