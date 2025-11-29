package com.ctxh.volunteer.module.student.dto.response;

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
public class StudentResponseDto {
    private Long studentId;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String mssv;
    private String academicYear;
    private String faculty;
    private Double totalCtxhDays;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String avatarUrl;
    private String bio;
    private String qrCodeData;
    private LocalDateTime qrCodeGeneratedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
