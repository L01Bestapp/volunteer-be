package com.ctxh.volunteer.module.activity.dto.request;

import com.ctxh.volunteer.common.util.EnumValidation;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateActivityRequestDto {
    @NotBlank(message = "Activity title is required")
    @Size(max = 200, message = "Activity title must not exceed 200 characters")
    private String title;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    private String description;

    @EnumValidation(name = "ActivityCategory", enumClass = ActivityCategory.class)
    private String category;

    @NotNull(message = "Start date time is required")
    @Future(message = "Start date time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @Future(message = "End date time must be in the future")
    private LocalDateTime endDateTime;

    private LocalDateTime registrationOpensAt;

    @Future(message = "Registration deadline must be in the future")
    private LocalDateTime registrationDeadline;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    private String requirements;

    @NotNull(message = "benefitsCtxh is required")
    @DecimalMin(value = "0.5", message = "benefitsCtxh must be at least 0.5")
    private Double benefitsCtxh;
}
