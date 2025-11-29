package com.ctxh.volunteer.module.attendance.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.attendance.dto.AttendanceResponseDto;
import com.ctxh.volunteer.module.attendance.dto.AttendanceSummaryDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckInRequestDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckOutRequestDto;
import com.ctxh.volunteer.module.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ============ ORGANIZER - ATTENDANCE MANAGEMENT ============

    /**
     * Check-in student using QR code
     * POST /api/v1/attendance/check-in
     */
    @Operation(summary = "check-in student using QR code")
    @PostMapping("/attendance/check-in")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AttendanceResponseDto> checkIn(
            @Valid @RequestBody QrCheckInRequestDto requestDto) {
        return ApiResponse.ok(
                "Student checked in successfully",
                attendanceService.checkIn(requestDto)
        );
    }

    /**
     * Check-out student using QR code
     * POST /api/v1/attendance/check-out
     */
    @Operation(summary = "check-out student using QR code")
    @PostMapping("/attendance/check-out")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AttendanceResponseDto> checkOut(
            @Valid @RequestBody QrCheckOutRequestDto requestDto) {
        return ApiResponse.ok(
                "Student checked out successfully",
                attendanceService.checkOut(requestDto)
        );
    }

    /**
     * Get all attendance records for an activity
     * GET /api/v1/activities/{activityId}/attendance
     */
    @Operation(summary = "get all attendance records for an activity")
    @GetMapping("/activities/{activityId}/attendance")
    public ApiResponse<List<AttendanceResponseDto>> getActivityAttendance(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Attendance records retrieved successfully",
                attendanceService.getActivityAttendance(activityId)
        );
    }

    /**
     * Get attendance summary statistics for an activity
     * GET /api/v1/activities/{activityId}/attendance/summary
     */
    @Operation(summary = "get attendance summary for an activity")
    @GetMapping("/activities/{activityId}/attendance/summary")
    public ApiResponse<AttendanceSummaryDto> getAttendanceSummary(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Attendance summary retrieved successfully",
                attendanceService.getAttendanceSummary(activityId)
        );
    }

    // ============ STUDENT - VIEW ATTENDANCE ============

    /**
     * Get student's attendance history
     * GET /api/v1/students/attendance/history
     */
    @Operation(summary = "get student's attendance history")
    @GetMapping("/students/attendance/history")
    public ApiResponse<List<AttendanceResponseDto>> getStudentAttendanceHistory(
            @RequestParam("studentId") Long studentId) {
        return ApiResponse.ok(
                "Attendance history retrieved successfully",
                attendanceService.getStudentAttendanceHistory(studentId)
        );
    }

    /**
     * Get student's attendance for a specific activity
     * GET /api/v1/activities/{activityId}/my-attendance
     */
    @Operation(summary = "get student's attendance for an activity")
    @GetMapping("/activities/{activityId}/my-attendance")
    public ApiResponse<AttendanceResponseDto> getStudentActivityAttendance(
            @PathVariable("activityId") Long activityId,
            @RequestParam("studentId") Long studentId) {
        return ApiResponse.ok(
                "Attendance record retrieved successfully",
                attendanceService.getStudentActivityAttendance(studentId, activityId)
        );
    }
}
