package com.ctxh.volunteer.module.organization.dto.response;

import com.ctxh.volunteer.module.student.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationListResponseDto {
    private Long studentId;
    private String fullName;
    private String mssv;
    private String faculty;
    private Double totalCtxhDays;
    private Gender gender;
    private String avatarUrl;
}
