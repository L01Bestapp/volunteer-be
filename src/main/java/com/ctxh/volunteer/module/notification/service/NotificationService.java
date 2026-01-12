package com.ctxh.volunteer.module.notification.service;

import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.notification.entity.Notification;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.repository.NotificationRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final ExpoPushService expoPushService;

    /**
     * Send and save notification
     *
     * @param userId User ID to send notification to
     * @param title  Notification title
     * @param body   Notification body
     * @param type   Notification type
     * @param data   Additional data
     * @return Saved notification
     */
    @Transactional
    public Notification sendAndSaveNotification(Long userId, String title, String body,
                                                NotificationType type, Map<String, Object> data) {
        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Save notification to database
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .data(data)
                .isRead(false)
                .isSent(false)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Saved notification to database: {}", notification.getNotificationId());

        // 3. Send push notification if user has token
        String pushToken = user.getFcmToken();
        if (pushToken != null && !pushToken.isEmpty()) {
            try {
                // Convert data to Map<String, String>
                Map<String, String> stringData = convertToStringMap(data);

                boolean success = false;

                // Detect token type and use appropriate service
                if (isExpoToken(pushToken)) {
                    // Use Expo Push Service for Expo tokens
                    log.info("Detected Expo push token, using ExpoPushService");
                    success = expoPushService.sendNotification(pushToken, title, body, stringData);
                } else {
                    // Use FCM Service for native FCM tokens
                    log.info("Detected native FCM token, using FCMService");
                    String messageId = fcmService.sendNotification(pushToken, title, body, stringData);
                    success = (messageId != null);
                }

                if (success) {
                    notification.markAsSent();
                    notificationRepository.save(notification);
                    log.info("Successfully sent push notification to user {}", userId);
                } else {
                    log.warn("Failed to send push notification to user {}", userId);
                }
            } catch (Exception e) {
                log.error("Error sending push notification to user {}: {}", userId, e.getMessage());
            }
        } else {
            log.warn("User {} does not have push token, skipping push notification", userId);
        }

        return notification;
    }

    /**
     * Send notification without additional data
     */
    @Transactional
    public Notification sendAndSaveNotification(Long userId, String title, String body, NotificationType type) {
        return sendAndSaveNotification(userId, title, body, type, null);
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserUserId(userId);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Count unread notifications for a user
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserUserIdAndIsReadFalse(userId);
        notifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(notifications);
    }

    /**
     * Check if reminder notification already sent for activity
     */
    @Transactional(readOnly = true)
    public boolean hasReminderBeenSent(Long userId, Long activityId) {
        return notificationRepository.findByUserIdAndTypeAndActivityId(
                userId,
                NotificationType.REMINDER.name(), // Convert enum to String
                String.valueOf(activityId)
        ).isPresent();
    }

    /**
     * Convert Map<String, Object> to Map<String, String> for push services
     */
    private Map<String, String> convertToStringMap(Map<String, Object> data) {
        if (data == null) {
            return new HashMap<>();
        }

        Map<String, String> stringMap = new HashMap<>();
        data.forEach((key, value) -> {
            if (value != null) {
                stringMap.put(key, value.toString());
            }
        });
        return stringMap;
    }

    /**
     * Check if token is an Expo push token
     */
    private boolean isExpoToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // Expo tokens have format: ExponentPushToken[xxx] or ExpoPushToken[xxx]
        return token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken[");
    }
}
