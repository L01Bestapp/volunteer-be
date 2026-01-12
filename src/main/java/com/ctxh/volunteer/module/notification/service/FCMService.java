package com.ctxh.volunteer.module.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    /**
     * Send notification via Firebase Cloud Messaging
     *
     * @param fcmToken FCM token of the device
     * @param title    Notification title
     * @param body     Notification body
     * @param data     Additional data to send with notification
     * @return Message ID if successful, null otherwise
     */
    public String sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("FCM token is null or empty, cannot send notification");
                return null;
            }

            // Build notification payload (displayed by OS)
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build Android Config (CRITICAL for Popup/Heads-up notification)
            // This ensures notifications appear as popup banner on Android devices
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH) // HIGH priority to trigger heads-up display
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("default") // Must match channel ID configured in React Native app
                            .setSound("default")     // Play default notification sound
                            .build())
                    .build();

            // Build message with Android Config
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig); // Add Android-specific configuration

            // Add data payload if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // Send message via Firebase
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message with Android config. Message ID: {}", response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to token {}: {}", fcmToken, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM notification: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Send notification without additional data
     */
    public String sendNotification(String fcmToken, String title, String body) {
        return sendNotification(fcmToken, title, body, null);
    }
}
