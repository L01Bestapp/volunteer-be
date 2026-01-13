package com.ctxh.volunteer.module.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FCMServiceTest {

    private FCMService fcmService;
    private MockedStatic<FirebaseMessaging> firebaseMessagingStatic;
    private FirebaseMessaging firebaseMessaging;

    @BeforeEach
    void setUp() {
        fcmService = new FCMService();
        firebaseMessaging = mock(FirebaseMessaging.class);
        firebaseMessagingStatic = mockStatic(FirebaseMessaging.class);
        firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
    }

    @AfterEach
    void tearDown() {
        firebaseMessagingStatic.close();
    }

    @Test
    @DisplayName("sendNotification returns null when token is null")
    void sendNotification_NullToken_ReturnsNull() {
        String result = fcmService.sendNotification(null, "Title", "Body");
        assertNull(result);
    }

    @Test
    @DisplayName("sendNotification returns message ID on success")
    void sendNotification_Success_ReturnsMessageId() throws FirebaseMessagingException {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-123");

        String result = fcmService.sendNotification("token-123", "Title", "Body", Map.of("key", "value"));

        assertEquals("message-123", result);
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("sendNotification returns null on FirebaseMessagingException")
    void sendNotification_FirebaseException_ReturnsNull() throws FirebaseMessagingException {
        when(firebaseMessaging.send(any(Message.class))).thenThrow(mock(FirebaseMessagingException.class));

        String result = fcmService.sendNotification("token-123", "Title", "Body");

        assertNull(result);
    }

    @Test
    @DisplayName("sendNotification returns null on unexpected Exception")
    void sendNotification_UnexpectedException_ReturnsNull() throws FirebaseMessagingException {
        // Mocking send to throw a generic exception
        when(firebaseMessaging.send(any(Message.class))).thenThrow(new RuntimeException("Unexpected"));

        String result = fcmService.sendNotification("token-123", "Title", "Body");

        assertNull(result);
    }
}
