package com.ctxh.volunteer.module.attendance.controller;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.attendance.dto.AttendanceResponseDto;
import com.ctxh.volunteer.module.attendance.dto.AttendanceSummaryDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckInRequestDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckOutRequestDto;
import com.ctxh.volunteer.module.attendance.enums.AttendanceStatus;
import com.ctxh.volunteer.module.attendance.service.AttendanceService;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AttendanceController.class,
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = com.ctxh.volunteer.module.auth.config.CustomAuthenticationConverter.class
    ),
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    })
@DisplayName("AttendanceController Integration Tests")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    private QrCheckInRequestDto checkInRequest;
    private QrCheckOutRequestDto checkOutRequest;
    private AttendanceResponseDto attendanceResponse;

    @BeforeEach
    void setUp() {
        // Setup check-in request
        checkInRequest = new QrCheckInRequestDto();
        checkInRequest.setQrCodeData("STUDENT-1-2012345");
        checkInRequest.setActivityId(1L);

        // Setup check-out request
        checkOutRequest = new QrCheckOutRequestDto();
        checkOutRequest.setQrCodeData("STUDENT-1-2012345");
        checkOutRequest.setActivityId(1L);

        // Setup response
        attendanceResponse = AttendanceResponseDto.builder()
                .attendanceId(1L)
                .studentId(1L)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .email("student@hcmut.edu.vn")
                .gender(Gender.MALE)
                .activityId(1L)
                .activityTitle("Test Activity")
                .organizationName("Test Org")
                .attendanceDate(LocalDateTime.now())
                .checkInTime(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    // ==================== CHECK-IN TESTS ====================

    @Test
    @DisplayName("POST /check-in - Success checks in student")
    void checkIn_Success_ReturnsOk() throws Exception {
        // Arrange
        when(attendanceService.checkIn(any(QrCheckInRequestDto.class)))
                .thenReturn(attendanceResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student checked in successfully"))
                .andExpect(jsonPath("$.data.attendanceId").value(1L))
                .andExpect(jsonPath("$.data.studentId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PRESENT"))
                .andExpect(jsonPath("$.data.checkInTime").exists());
    }

    @Test
    @DisplayName("POST /check-in - Fails with invalid QR code")
    void checkIn_InvalidQrCode_ReturnsBadRequest() throws Exception {
        // Arrange
        when(attendanceService.checkIn(any(QrCheckInRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_QR_CODE));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /check-in - Fails when already checked in")
    void checkIn_AlreadyCheckedIn_ReturnsConflict() throws Exception {
        // Arrange
        when(attendanceService.checkIn(any(QrCheckInRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.ALREADY_CHECKED_IN));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /check-in - Fails when student not enrolled")
    void checkIn_StudentNotEnrolled_ReturnsForbidden() throws Exception {
        // Arrange
        when(attendanceService.checkIn(any(QrCheckInRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /check-in - Fails with missing required fields")
    void checkIn_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Arrange
        QrCheckInRequestDto invalidRequest = new QrCheckInRequestDto();

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== CHECK-OUT TESTS ====================

    @Test
    @DisplayName("POST /check-out - Success checks out student")
    void checkOut_Success_ReturnsOk() throws Exception {
        // Arrange
        attendanceResponse.setCheckOutTime(LocalDateTime.now());
        attendanceResponse.setDurationMinutes(120L);

        when(attendanceService.checkOut(any(QrCheckOutRequestDto.class)))
                .thenReturn(attendanceResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student checked out successfully"))
                .andExpect(jsonPath("$.data.attendanceId").value(1L))
                .andExpect(jsonPath("$.data.checkOutTime").exists())
                .andExpect(jsonPath("$.data.durationMinutes").value(120));
    }

    @Test
    @DisplayName("POST /check-out - Fails with invalid QR code")
    void checkOut_InvalidQrCode_ReturnsBadRequest() throws Exception {
        // Arrange
        when(attendanceService.checkOut(any(QrCheckOutRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_QR_CODE));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /check-out - Fails when not checked in")
    void checkOut_NotCheckedIn_ReturnsBadRequest() throws Exception {
        // Arrange
        when(attendanceService.checkOut(any(QrCheckOutRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_CHECKED_IN));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /check-out - Fails when already checked out")
    void checkOut_AlreadyCheckedOut_ReturnsConflict() throws Exception {
        // Arrange
        when(attendanceService.checkOut(any(QrCheckOutRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.ALREADY_CHECKED_OUT));

        // Act & Assert
        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET ACTIVITY ATTENDANCE TESTS ====================

    @Test
    @DisplayName("GET /activities/{id}/attendance - Success returns list")
    void getActivityAttendance_Success_ReturnsList() throws Exception {
        // Arrange
        when(attendanceService.getActivityAttendance(1L))
                .thenReturn(List.of(attendanceResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/1/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance records retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].attendanceId").value(1L))
                .andExpect(jsonPath("$.data[0].studentId").value(1L));
    }

    @Test
    @DisplayName("GET /activities/{id}/attendance - Returns empty list when no attendances")
    void getActivityAttendance_ReturnsEmpty_WhenNoAttendances() throws Exception {
        // Arrange
        when(attendanceService.getActivityAttendance(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/1/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /activities/{id}/attendance - Fails when activity not found")
    void getActivityAttendance_ActivityNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(attendanceService.getActivityAttendance(999L))
                .thenThrow(new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/999/attendance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET ATTENDANCE SUMMARY TESTS ====================

    @Test
    @DisplayName("GET /activities/{id}/attendance/summary - Success returns summary")
    void getAttendanceSummary_Success_ReturnsSummary() throws Exception {
        // Arrange
        AttendanceSummaryDto summaryDto = AttendanceSummaryDto.builder()
                .activityId(1L)
                .activityTitle("Test Activity")
                .totalEnrolled(10L)
                .totalPresent(8L)
                .totalAbsent(2L)
                .totalCheckedIn(8L)
                .totalCheckedOut(5L)
                .attendanceRate(80.0)
                .build();

        when(attendanceService.getAttendanceSummary(1L))
                .thenReturn(summaryDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/1/attendance/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance summary retrieved successfully"))
                .andExpect(jsonPath("$.data.activityId").value(1L))
                .andExpect(jsonPath("$.data.totalEnrolled").value(10))
                .andExpect(jsonPath("$.data.totalPresent").value(8))
                .andExpect(jsonPath("$.data.totalAbsent").value(2))
                .andExpect(jsonPath("$.data.attendanceRate").value(80.0));
    }

    @Test
    @DisplayName("GET /activities/{id}/attendance/summary - Fails when activity not found")
    void getAttendanceSummary_ActivityNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(attendanceService.getAttendanceSummary(999L))
                .thenThrow(new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/999/attendance/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET STUDENT ATTENDANCE HISTORY TESTS ====================

    @Test
    @DisplayName("GET /students/attendance/history - Success returns history")
    void getStudentAttendanceHistory_Success_ReturnsHistory() throws Exception {
        // Arrange
        when(attendanceService.getStudentAttendanceHistory(1L))
                .thenReturn(List.of(attendanceResponse));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/attendance/history")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance history retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].studentId").value(1L))
                .andExpect(jsonPath("$.data[0].activityId").value(1L));
    }

    @Test
    @DisplayName("GET /students/attendance/history - Returns empty when no history")
    void getStudentAttendanceHistory_ReturnsEmpty_WhenNoHistory() throws Exception {
        // Arrange
        when(attendanceService.getStudentAttendanceHistory(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/attendance/history")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /students/attendance/history - Fails when student not found")
    void getStudentAttendanceHistory_StudentNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(attendanceService.getStudentAttendanceHistory(999L))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/attendance/history")
                        .param("studentId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET STUDENT ACTIVITY ATTENDANCE TESTS ====================

    @Test
    @DisplayName("GET /activities/{id}/my-attendance - Success returns attendance")
    void getStudentActivityAttendance_Success_ReturnsAttendance() throws Exception {
        // Arrange
        when(attendanceService.getStudentActivityAttendance(1L, 1L))
                .thenReturn(attendanceResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/1/my-attendance")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance record retrieved successfully"))
                .andExpect(jsonPath("$.data.attendanceId").value(1L))
                .andExpect(jsonPath("$.data.studentId").value(1L))
                .andExpect(jsonPath("$.data.activityId").value(1L));
    }

    @Test
    @DisplayName("GET /activities/{id}/my-attendance - Fails when student not found")
    void getStudentActivityAttendance_StudentNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(attendanceService.getStudentActivityAttendance(999L, 1L))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/1/my-attendance")
                        .param("studentId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /activities/{id}/my-attendance - Fails when attendance not found")
    void getStudentActivityAttendance_AttendanceNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(attendanceService.getStudentActivityAttendance(1L, 999L))
                .thenThrow(new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/activities/999/my-attendance")
                        .param("studentId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
