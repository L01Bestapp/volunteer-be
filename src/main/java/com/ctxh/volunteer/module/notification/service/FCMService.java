package com.ctxh.volunteer.module.notification.service;

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

            // Build notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            // Add data if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message. Message ID: {}", response);
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
