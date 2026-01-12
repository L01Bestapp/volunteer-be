package com.ctxh.volunteer.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @Bean
    public FirebaseApp initializeFirebase() {
        try {
            // Check if FirebaseApp is already initialized
            if (FirebaseApp.getApps().isEmpty()) {

                // Check if Firebase credentials are provided
                if (firebaseServiceAccountJson == null || firebaseServiceAccountJson.trim().isEmpty()) {
                    log.warn("Firebase credentials not found in environment variables");
                    log.warn("Set FIREBASE_SERVICE_ACCOUNT_JSON environment variable to enable FCM notifications");
                    return null;
                }

                // Convert JSON string to InputStream
                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(
                        firebaseServiceAccountJson.getBytes(StandardCharsets.UTF_8)
                );

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully from environment variables");
                return firebaseApp;
            } else {
                log.info("FirebaseApp already initialized");
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.warn("FCM notifications will not work. Please check FIREBASE_SERVICE_ACCOUNT_JSON environment variable");
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while initializing Firebase: {}", e.getMessage(), e);
            return null;
        }
    }
}
