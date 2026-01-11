package com.ctxh.volunteer.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.file:classpath:firebase-service-account.json}")
    private Resource firebaseConfigResource;

    @Bean
    public FirebaseApp initializeFirebase() {
        try {
            // Check if FirebaseApp is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = firebaseConfigResource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
                return firebaseApp;
            } else {
                log.info("FirebaseApp already initialized");
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.warn("FCM notifications will not work. Please add firebase-service-account.json to resources folder");
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while initializing Firebase: {}", e.getMessage(), e);
            return null;
        }
    }
}
