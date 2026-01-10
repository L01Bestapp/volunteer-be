package com.ctxh.volunteer.module.enrollment.dto;

import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyActivityResponseDto {

    // Enrollment info
    private Long enrollmentId;
    private EnrollmentStatus enrollmentStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private Boolean isCompleted;
    private LocalDateTime completedAt;

    // Activity info
    private Long activityId;
    private String activityTitle;
    private String shortDescription;
    private ActivityCategory category;
    private RegistrationState registrationState;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String address;
    private Double benefitsCtxh;

    // Organization info
    private Long organizationId;
    private String organizationName;
}
