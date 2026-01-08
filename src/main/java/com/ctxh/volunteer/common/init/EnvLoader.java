package com.ctxh.volunteer.common.init;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private EnvLoader() {}

    public static void initEnv() {
        // Load .env file if exists (for local development)
        // In production (Render, Railway, etc.), env vars are provided by platform
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't throw exception if .env file is missing
                    .load();

            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            // Silently ignore - production environments use platform env vars
            System.out.println("No .env file found - using platform environment variables");
        }
    }
}