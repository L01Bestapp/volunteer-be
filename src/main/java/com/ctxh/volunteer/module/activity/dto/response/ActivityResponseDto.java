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
public class ActivityResponseDto {
    private Long activityId;
    private Long organizationId;
    private String organizationName;
    private String imageUrl;
    private String name;
    private String description;
    private String shortDescription;
    private ActivityCategory category;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime registrationOpensAt;
    private LocalDateTime registrationDeadline;
    private String address;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer pendingParticipants;
    private Integer approvedParticipants;
    private Integer remainingSlots;
    private RegistrationState registrationState;
    private ActivityStatus activityStatus;
    private String requirements;
    private Double benefitsCtxh;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
