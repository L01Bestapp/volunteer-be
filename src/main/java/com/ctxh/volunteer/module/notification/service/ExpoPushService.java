package com.ctxh.volunteer.module.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to send push notifications via Expo Push Notification Service
 * Use this for Expo apps instead of FCMService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final WebClient.Builder webClientBuilder;

    /**
     * Send notification via Expo Push Notification Service
     *
     * @param expoPushToken Expo push token (format: ExponentPushToken[xxx] or ExpoPushToken:xxx)
     * @param title         Notification title
     * @param body          Notification body
     * @param data          Additional data to send with notification
     * @return true if successful, false otherwise
     */
    public boolean sendNotification(String expoPushToken, String title, String body, Map<String, String> data) {
        try {
            if (expoPushToken == null || expoPushToken.isEmpty()) {
                log.warn("Expo push token is null or empty, cannot send notification");
                return false;
            }

            // Validate Expo token format
            if (!isValidExpoToken(expoPushToken)) {
                log.warn("Invalid Expo push token format: {}", expoPushToken);
                return false;
            }

            // Build notification message according to Expo Push API spec
            Map<String, Object> message = new HashMap<>();
            message.put("to", expoPushToken);
            message.put("title", title);
            message.put("body", body);
            message.put("sound", "default");

            // IMPORTANT: priority and channelId must be at ROOT level for Android heads-up
            // Expo will automatically map these to Android-specific settings
            message.put("priority", "high");      // Required for heads-up notification
            message.put("channelId", "default");  // Must match channel ID configured in app

            // Add data if provided
            if (data != null && !data.isEmpty()) {
                message.put("data", data);
            }

            // Send to Expo Push Service
            WebClient webClient = webClientBuilder.build();

            Map<String, Object> response = webClient.post()
                    .uri(EXPO_PUSH_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(List.of(message)) // Expo API expects array of messages
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("data");
                if (!results.isEmpty()) {
                    Map<String, Object> result = results.get(0);
                    if ("ok".equals(result.get("status"))) {
                        String ticketId = (String) result.get("id");
                        log.info("Successfully sent Expo push notification. Ticket ID: {}", ticketId);
                        return true;
                    } else {
                        String errorMessage = result.containsKey("message") ? (String) result.get("message") : "Unknown error";
                        log.error("Failed to send Expo push notification. Error: {}", errorMessage);
                        return false;
                    }
                }
            }

            log.error("Unexpected response from Expo Push Service: {}", response);
            return false;

        } catch (Exception e) {
            log.error("Error sending Expo push notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send notification without additional data
     */
    public boolean sendNotification(String expoPushToken, String title, String body) {
        return sendNotification(expoPushToken, title, body, null);
    }

    /**
     * Send notification to multiple tokens at once (batch send)
     */
    public Map<String, Boolean> sendBatchNotifications(List<String> expoPushTokens, String title, String body, Map<String, String> data) {
        Map<String, Boolean> results = new HashMap<>();

        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            for (String token : expoPushTokens) {
                if (!isValidExpoToken(token)) {
                    results.put(token, false);
                    continue;
                }

                Map<String, Object> message = new HashMap<>();
                message.put("to", token);
                message.put("title", title);
                message.put("body", body);
                message.put("sound", "default");

                // IMPORTANT: priority and channelId at ROOT level
                message.put("priority", "high");
                message.put("channelId", "default");

                if (data != null && !data.isEmpty()) {
                    message.put("data", data);
                }

                messages.add(message);
            }

            if (messages.isEmpty()) {
                log.warn("No valid tokens to send notifications to");
                return results;
            }

            // Send batch request to Expo
            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.post()
                    .uri(EXPO_PUSH_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(messages)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> responseResults = (List<Map<String, Object>>) response.get("data");
                for (int i = 0; i < responseResults.size(); i++) {
                    Map<String, Object> result = responseResults.get(i);
                    String token = expoPushTokens.get(i);
                    results.put(token, "ok".equals(result.get("status")));
                }
            }

            log.info("Sent batch notifications to {} tokens. Success: {}, Failed: {}",
                    expoPushTokens.size(),
                    results.values().stream().filter(v -> v).count(),
                    results.values().stream().filter(v -> !v).count());

        } catch (Exception e) {
            log.error("Error sending batch notifications: {}", e.getMessage(), e);
            // Mark all as failed
            expoPushTokens.forEach(token -> results.put(token, false));
        }

        return results;
    }

    /**
     * Validate if token is a valid Expo push token
     */
    private boolean isValidExpoToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Valid formats:
        // - ExponentPushToken[xxx]
        // - ExpoPushToken[xxx]
        // - expo-push-token-format (for testing)
        return token.startsWith("ExponentPushToken[") ||
               token.startsWith("ExpoPushToken[") ||
               token.matches("^[a-zA-Z0-9_-]+$"); // Allow simple alphanumeric for testing
    }
}
