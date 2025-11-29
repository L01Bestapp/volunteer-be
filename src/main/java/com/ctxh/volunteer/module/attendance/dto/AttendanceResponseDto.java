package com.ctxh.volunteer.module.attendance.dto;

import com.ctxh.volunteer.module.attendance.enums.AttendanceStatus;
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
public class AttendanceResponseDto {

    // Attendance info
    private Long attendanceId;
    private LocalDateTime attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private Long durationMinutes;

    // Student info
    private Long studentId;
    private String fullName;
    private String mssv;
    private String email;
    private String phoneNumber;
    private String academicYear;
    private String faculty;
    private Gender gender;
    private LocalDate dateOfBirth;

    // Activity info (for student history)
    private Long activityId;
    private String activityTitle;
    private String organizationName;
}
