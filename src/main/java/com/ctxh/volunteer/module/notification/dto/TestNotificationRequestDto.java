package com.ctxh.volunteer.module.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestNotificationRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private String type; // Optional: REMINDER, ATTENDANCE_COMPLETED, GENERAL, etc.

    private String activityId; // Optional: for testing navigation

    private String customData; // Optional: any custom JSON data
}
