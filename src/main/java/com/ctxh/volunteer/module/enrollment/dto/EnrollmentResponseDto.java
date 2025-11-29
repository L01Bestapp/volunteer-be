package com.ctxh.volunteer.module.enrollment.dto;

import com.ctxh.volunteer.module.enrollment.entity.Enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.student.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponseDto {

    // Enrollment information
    private Long enrollmentId;
    private EnrollmentStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private LocalDateTime rejectedAt;
    private Long rejectedBy;
    private Boolean isCompleted;
    private LocalDateTime completedAt;

    // Student information
    private Long studentId;
    private String fullName;
    private String mssv;
    private String email;
    private String phoneNumber;
    private String academicYear;
    private String faculty;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Double totalCtxhDays;
}
