package com.ctxh.volunteer.module.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDto {

    private Long activityId;
    private String activityTitle;

    // Enrollment stats
    private Long totalEnrolled;      // Total approved students

    // Attendance stats
    private Long totalPresent;       // Students marked present
    private Long totalAbsent;        // Students marked absent
    private Long totalCheckedIn;     // Students who checked in
    private Long totalCheckedOut;    // Students who checked out

    // Percentage
    private Double attendanceRate;   // (totalPresent / totalEnrolled) * 100
}
