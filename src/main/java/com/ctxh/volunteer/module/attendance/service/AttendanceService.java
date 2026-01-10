package com.ctxh.volunteer.module.attendance.service;

import com.ctxh.volunteer.module.attendance.dto.AttendanceResponseDto;
import com.ctxh.volunteer.module.attendance.dto.AttendanceSummaryDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckInRequestDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckOutRequestDto;
import jakarta.validation.Valid;

import java.util.List;

public interface AttendanceService {

    // ============ ORGANIZER - ATTENDANCE MANAGEMENT ============

    /**
     * Check-in student using QR code
     */
    AttendanceResponseDto checkIn(QrCheckInRequestDto requestDto);

    /**
     * Check out student using QR code
     */
    AttendanceResponseDto checkOut(@Valid QrCheckOutRequestDto requestDto);

    /**
     * Get all attendance records for an activity
     */
    List<AttendanceResponseDto> getActivityAttendance(Long activityId);

    /**
     * Get attendance summary statistics for an activity
     */
    AttendanceSummaryDto getAttendanceSummary(Long activityId);

    // ============ STUDENT - VIEW ATTENDANCE ============

    /**
     * Get student's attendance history
     */
    List<AttendanceResponseDto> getStudentAttendanceHistory(Long studentId);

    /**
     * Get student's attendance for a specific activity
     */
    AttendanceResponseDto getStudentActivityAttendance(Long studentId, Long activityId);
}
