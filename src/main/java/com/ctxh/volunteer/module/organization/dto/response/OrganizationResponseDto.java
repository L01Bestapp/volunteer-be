package com.ctxh.volunteer.module.organization.dto.response;

import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import com.ctxh.volunteer.module.student.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {
    private Long organizationId;
    private String email;
    private String organizationName;
    private String avatarUrl;
    private String bio;
    private String representativeName;
    private String representativeEmail;
    private String representativePhoneNumber;
    private OrganizationType type;
    private LocalDateTime createdAt;
}
