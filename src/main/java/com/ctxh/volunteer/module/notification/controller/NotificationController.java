package com.ctxh.volunteer.module.notification.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.common.util.AuthUtil;
import com.ctxh.volunteer.module.notification.dto.NotificationResponseDto;
import com.ctxh.volunteer.module.notification.dto.TestNotificationRequestDto;
import com.ctxh.volunteer.module.notification.dto.UpdateFcmTokenRequestDto;
import com.ctxh.volunteer.module.notification.entity.Notification;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.service.NotificationService;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Get all notifications for current user
     * GET /api/v1/notifications
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all notifications for current user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponseDto>> getAllNotifications() {
        Long userId = AuthUtil.getIdFromAuthentication();

        List<Notification> notifications = notificationService.getUserNotifications(userId);
        List<NotificationResponseDto> response = notifications.stream()
                .map(this::mapToDto)
                .toList();

        return ApiResponse.ok(
                "Notifications retrieved successfully",
                response
        );
    }

    /**
     * Get unread notifications for current user
     * GET /api/v1/notifications/unread
     */
    @GetMapping("/unread")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get unread notifications for current user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponseDto>> getUnreadNotifications() {
        Long userId = AuthUtil.getIdFromAuthentication();

        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        List<NotificationResponseDto> response = notifications.stream()
                .map(this::mapToDto)
                .toList();

        return ApiResponse.ok(
                "Unread notifications retrieved successfully",
                response
        );
    }

    /**
     * Get count of unread notifications for current user
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/unread-count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get count of unread notifications for current user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = AuthUtil.getIdFromAuthentication();
        Long count = notificationService.countUnreadNotifications(userId);

        return ApiResponse.ok(
                "Unread count retrieved successfully",
                count
        );
    }

    /**
     * Mark notification as read
     * PUT /api/v1/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark notification as read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ApiResponse.ok("Notification marked as read");
    }

    /**
     * Mark all notifications as read for current user
     * PUT /api/v1/notifications/read-all
     */
    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mark all notifications as read for current user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = AuthUtil.getIdFromAuthentication();
        notificationService.markAllAsRead(userId);

        return ApiResponse.ok("All notifications marked as read");
    }

    /**
     * Update FCM token for current user
     * PUT /api/v1/notifications/fcm-token
     */
    @PutMapping("/fcm-token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update FCM token for current user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateFcmToken(@Valid @RequestBody UpdateFcmTokenRequestDto requestDto) {
        Long userId = AuthUtil.getIdFromAuthentication();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFcmToken(requestDto.getFcmToken());
        userRepository.save(user);

        log.info("Updated FCM token for user {}", userId);
        return ApiResponse.ok("FCM token updated successfully");
    }

    /**
     * Send test notification to current user (for testing/debugging)
     * POST /api/v1/notifications/test
     */
    @PostMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Send test notification to current user (for testing purposes)")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> sendTestNotification(@Valid @RequestBody TestNotificationRequestDto requestDto) {
        Long userId = AuthUtil.getIdFromAuthentication();

        // Prepare data payload
        Map<String, Object> data = new HashMap<>();

        // Add type
        String type = requestDto.getType() != null ? requestDto.getType() : "GENERAL";

        // Add activityId if provided
        if (requestDto.getActivityId() != null && !requestDto.getActivityId().isEmpty()) {
            data.put("activityId", requestDto.getActivityId());
        }

        // Add custom data if provided
        if (requestDto.getCustomData() != null && !requestDto.getCustomData().isEmpty()) {
            data.put("customData", requestDto.getCustomData());
        }

        // Add test flag
        data.put("isTest", true);

        // Determine notification type
        NotificationType notificationType;
        try {
            notificationType = NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            notificationType = NotificationType.GENERAL;
        }

        // Send notification
        notificationService.sendAndSaveNotification(
                userId,
                requestDto.getTitle(),
                requestDto.getBody(),
                notificationType,
                data
        );

        log.info("Sent test notification to user {}", userId);
        return ApiResponse.ok("Test notification sent successfully");
    }

    // ============ MAPPING METHODS ============

    private NotificationResponseDto mapToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .isSent(notification.getIsSent())
                .createAt(notification.getCreateAt())
                .build();
    }
}
