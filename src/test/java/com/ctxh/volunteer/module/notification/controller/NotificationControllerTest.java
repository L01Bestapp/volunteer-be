package com.ctxh.volunteer.module.notification.controller;

import com.ctxh.volunteer.common.exception.GlobalExceptionHandler;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.notification.dto.TestNotificationRequestDto;
import com.ctxh.volunteer.module.notification.dto.UpdateFcmTokenRequestDto;
import com.ctxh.volunteer.module.notification.entity.Notification;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {NotificationController.class, GlobalExceptionHandler.class},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.ctxh.volunteer.module.auth.config.CustomAuthenticationConverter.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@DisplayName("NotificationController Integration Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    private Notification notification;
    private User user;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

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
                .isSent(true)
                .data(Map.of("key", "value"))
                .build();
        notification.setCreateAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Success")
    void getAllNotifications_Success_ReturnsList() throws Exception {
        when(notificationService.getUserNotifications(anyLong())).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].notificationId").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread - Success")
    void getUnreadNotifications_Success_ReturnsList() throws Exception {
        when(notificationService.getUnreadNotifications(anyLong())).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/v1/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count - Success")
    void getUnreadCount_Success_ReturnsCount() throws Exception {
        when(notificationService.countUnreadNotifications(anyLong())).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read - Success")
    void markAsRead_Success_ReturnsOk() throws Exception {
        doNothing().when(notificationService).markAsRead(anyLong());

        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/read-all - Success")
    void markAllAsRead_Success_ReturnsOk() throws Exception {
        doNothing().when(notificationService).markAllAsRead(anyLong());

        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/fcm-token - Success")
    void updateFcmToken_Success_ReturnsOk() throws Exception {
        UpdateFcmTokenRequestDto requestDto = new UpdateFcmTokenRequestDto();
        requestDto.setFcmToken("new-token");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/v1/notifications/fcm-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/test - Success")
    void sendTestNotification_Success_ReturnsOk() throws Exception {
        TestNotificationRequestDto requestDto = new TestNotificationRequestDto();
        requestDto.setTitle("Test");
        requestDto.setBody("Body");
        requestDto.setType("GENERAL");

        when(notificationService.sendAndSaveNotification(anyLong(), anyString(), anyString(), any(), any()))
                .thenReturn(notification);

        mockMvc.perform(post("/api/v1/notifications/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
