package com.ctxh.volunteer.module.notification.dto;

import com.ctxh.volunteer.module.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private String title;
    private String body;
    private NotificationType type;
    private Map<String, Object> data;
    private Boolean isRead;
    private Boolean isSent;
    private LocalDateTime createAt;
}
