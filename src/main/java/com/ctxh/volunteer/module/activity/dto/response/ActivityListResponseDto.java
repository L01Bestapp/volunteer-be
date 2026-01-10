package com.ctxh.volunteer.module.activity.dto.response;

import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListResponseDto {
    private Long activityId;
    private String title;
    private String shortDescription;
    private String imageUrl;
    private ActivityCategory category;
    private LocalDateTime registrationDeadline;
    private Double theNumberOfCtxhDay;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String address;
    private Integer maxParticipants;
    private Integer approvedParticipants;
    private Integer remainingSlots;
    private RegistrationState registrationState;
    private ActivityStatus activityStatus;
    private LocalDateTime createdAt;
}
