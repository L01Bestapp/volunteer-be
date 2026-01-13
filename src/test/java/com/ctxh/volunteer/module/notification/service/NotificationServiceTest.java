package com.ctxh.volunteer.module.notification.service;

import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.notification.entity.Notification;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FCMService fcmService;

    @Mock
    private ExpoPushService expoPushService;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .email("test@email.com")
                .build();

        notification = Notification.builder()
                .notificationId(1L)
                .user(user)
                .title("Test Title")
                .body("Test Body")
                .type(NotificationType.GENERAL)
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("sendAndSaveNotification throws Exception when user not found")
    void sendAndSaveNotification_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                notificationService.sendAndSaveNotification(1L, "Title", "Body", NotificationType.GENERAL));
    }

    @Test
    @DisplayName("sendAndSaveNotification saves notification but skips push when token is null")
    void sendAndSaveNotification_NoToken_SavesOnly() {
        user.setFcmToken(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.sendAndSaveNotification(1L, "Title", "Body", NotificationType.GENERAL);

        assertNotNull(result);
        verify(expoPushService, never()).sendNotification(anyString(), anyString(), anyString(), anyMap());
        verify(fcmService, never()).sendNotification(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("sendAndSaveNotification sends Expo notification when token is Expo format")
    void sendAndSaveNotification_ExpoToken_SendsExpoPush() {
        user.setFcmToken("ExponentPushToken[abc]");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(expoPushService.sendNotification(anyString(), anyString(), anyString(), anyMap())).thenReturn(true);

        Notification result = notificationService.sendAndSaveNotification(1L, "Title", "Body", NotificationType.GENERAL, new HashMap<>());

        assertNotNull(result);
        verify(expoPushService).sendNotification(eq("ExponentPushToken[abc]"), eq("Title"), eq("Body"), anyMap());
        verify(fcmService, never()).sendNotification(anyString(), anyString(), anyString(), anyMap());
        // Should mark as sent
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    @DisplayName("sendAndSaveNotification sends FCM notification when token is FCM format")
    void sendAndSaveNotification_FcmToken_SendsFcmPush() {
        user.setFcmToken("fcm-token-123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(fcmService.sendNotification(anyString(), anyString(), anyString(), anyMap())).thenReturn("msg-id");

        Notification result = notificationService.sendAndSaveNotification(1L, "Title", "Body", NotificationType.GENERAL);

        assertNotNull(result);
        verify(fcmService).sendNotification(eq("fcm-token-123"), eq("Title"), eq("Body"), anyMap());
        verify(expoPushService, never()).sendNotification(anyString(), anyString(), anyString(), anyMap());
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    @DisplayName("getUserNotifications returns list")
    void getUserNotifications_ReturnsList() {
        when(notificationRepository.findByUserUserId(1L)).thenReturn(List.of(notification));

        List<Notification> result = notificationService.getUserNotifications(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getUnreadNotifications returns list")
    void getUnreadNotifications_ReturnsList() {
        when(notificationRepository.findByUserUserIdAndIsReadFalse(1L)).thenReturn(List.of(notification));

        List<Notification> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("countUnreadNotifications returns count")
    void countUnreadNotifications_ReturnsCount() {
        when(notificationRepository.countByUserUserIdAndIsReadFalse(1L)).thenReturn(5L);

        Long count = notificationService.countUnreadNotifications(1L);

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("markAsRead marks notification as read")
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead throws exception when not found")
    void markAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(1L));
    }

    @Test
    @DisplayName("markAllAsRead marks list as read")
    void markAllAsRead_Success() {
        Notification n1 = Notification.builder().isRead(false).build();
        Notification n2 = Notification.builder().isRead(false).build();
        when(notificationRepository.findByUserUserIdAndIsReadFalse(1L)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(1L);

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("hasReminderBeenSent returns true if found")
    void hasReminderBeenSent_ReturnsTrue() {
        when(notificationRepository.findByUserIdAndTypeAndActivityId(1L, "REMINDER", "100"))
                .thenReturn(Optional.of(notification));

        assertTrue(notificationService.hasReminderBeenSent(1L, 100L));
    }
}
