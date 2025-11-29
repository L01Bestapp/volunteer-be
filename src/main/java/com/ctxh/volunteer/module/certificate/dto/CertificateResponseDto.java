package com.ctxh.volunteer.module.certificate.dto;

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
public class CertificateResponseDto {

    // Certificate info
    private Long certificateId;
    private String certificateCode;
    private LocalDateTime issuedDate;
    private Boolean isRevoked;

    // Student info (for certificate generation)
    private Long studentId;
    private String studentName;
    private String studentMssv;
    private String studentFaculty;
    private String studentAcademicYear;
    private LocalDate studentDateOfBirth;
    private String studentGender;

    // Activity info (for certificate generation)
    private Long activityId;
    private String activityTitle;
    private LocalDateTime activityStartDate;
    private LocalDateTime activityEndDate;
    private Double ctxhHours;

    // Organization info (for certificate generation)
    private String organizationName;
    private String organizationAddress;
    private String organizationContact;

    // Enrollment info
    private Long enrollmentId;
    private LocalDateTime completedAt;

    // Formatted dates for display
    private String issuedDateFormatted;
    private String activityPeriodFormatted;
}
