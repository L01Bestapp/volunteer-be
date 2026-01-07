package com.ctxh.volunteer.module.attendance.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.attendance.dto.AttendanceResponseDto;
import com.ctxh.volunteer.module.attendance.dto.AttendanceSummaryDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckInRequestDto;
import com.ctxh.volunteer.module.attendance.dto.QrCheckOutRequestDto;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.attendance.enums.AttendanceStatus;
import com.ctxh.volunteer.module.attendance.repository.AttendanceRepository;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.certificate.service.CertificateService;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Unit Tests")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Student testStudent;
    private User testUser;
    private Activity testActivity;
    private Organization testOrganization;
    private Enrollment testEnrollment;
    private Attendance testAttendance;
    private QrCheckInRequestDto checkInRequest;
    private QrCheckOutRequestDto checkOutRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .userId(1L)
                .email("student@hcmut.edu.vn")
                .build();

        // Create test student
        testStudent = Student.builder()
                .studentId(1L)
                .user(testUser)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .gender(Gender.MALE)
                .qrCodeData("STUDENT-1-2012345")
                .build();

        testUser.setStudent(testStudent);

        // Create test organization
        testOrganization = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Org")
                .build();

        // Create test activity
        testActivity = Activity.builder()
                .activityId(1L)
                .title("Test Activity")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();

        // Create test enrollment
        testEnrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(testActivity)
                .status(EnrollmentStatus.APPROVED)
                .build();

        // Create test attendance
        testAttendance = Attendance.builder()
                .attendanceId(1L)
                .student(testStudent)
                .activity(testActivity)
                .attendanceDate(LocalDateTime.now())
                .status(AttendanceStatus.ABSENT)
                .build();

        // Setup request DTOs
        checkInRequest = new QrCheckInRequestDto();
        checkInRequest.setQrCodeData("STUDENT-1-2012345");
        checkInRequest.setActivityId(1L);

        checkOutRequest = new QrCheckOutRequestDto();
        checkOutRequest.setQrCodeData("STUDENT-1-2012345");
        checkOutRequest.setActivityId(1L);
    }

    // ==================== CHECK-IN TESTS ====================

    @Test
    @DisplayName("Check-in - Success creates new attendance record")
    void checkIn_Success_CreatesNewAttendance() {
        // Arrange
        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> {
                    Attendance attendance = invocation.getArgument(0);
                    attendance.setAttendanceId(1L);
                    return attendance;
                });

        // Act
        AttendanceResponseDto result = attendanceService.checkIn(checkInRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getCheckInTime()).isNotNull();

        verify(studentRepository).findByQrCodeData("STUDENT-1-2012345");
        verify(activityRepository).findById(1L);
        verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Check-in - Success updates existing attendance record")
    void checkIn_Success_UpdatesExistingAttendance() {
        // Arrange
        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(testAttendance);

        // Act
        AttendanceResponseDto result = attendanceService.checkIn(checkInRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(attendanceRepository).save(testAttendance);
    }

    @Test
    @DisplayName("Check-in - Fails with invalid QR code")
    void checkIn_InvalidQrCode_ThrowsException() {
        // Arrange
        when(studentRepository.findByQrCodeData("INVALID-QR"))
                .thenReturn(Optional.empty());

        checkInRequest.setQrCodeData("INVALID-QR");

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_QR_CODE);

        verify(studentRepository).findByQrCodeData("INVALID-QR");
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-in - Fails when activity not found")
    void checkIn_ActivityNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(999L))
                .thenReturn(Optional.empty());

        checkInRequest.setActivityId(999L);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findById(999L);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-in - Fails when student not enrolled")
    void checkIn_StudentNotEnrolled_ThrowsException() {
        // Arrange
        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_ENROLLED);

        verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-in - Fails when enrollment not approved")
    void checkIn_EnrollmentNotApproved_ThrowsException() {
        // Arrange
        testEnrollment.setStatus(EnrollmentStatus.PENDING);

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_ENROLLED);

        verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-in - Fails when already checked in")
    void checkIn_AlreadyCheckedIn_ThrowsException() {
        // Arrange
        testAttendance.checkIn(); // Already checked in

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CHECKED_IN);

        verify(attendanceRepository, never()).save(any());
    }

    // ==================== CHECK-OUT TESTS ====================

    @Test
    @DisplayName("Check-out - Success and auto-generates certificate")
    void checkOut_Success_GeneratesCertificate() {
        // Arrange
        testAttendance.checkIn(); // Must check in first

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(testAttendance);
        when(certificateService.certificateExists(1L)).thenReturn(false);
        when(certificateService.generateCertificate(any(Enrollment.class)))
                .thenReturn(mock(Certificate.class));

        // Act
        AttendanceResponseDto result = attendanceService.checkOut(checkOutRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCheckOutTime()).isNotNull();
        assertThat(result.getDurationMinutes()).isNotNull();

        verify(attendanceRepository).save(testAttendance);
        verify(enrollmentRepository).save(testEnrollment);
        verify(certificateService).certificateExists(1L);
        verify(certificateService).generateCertificate(testEnrollment);
    }

    @Test
    @DisplayName("Check-out - Skips certificate if already exists")
    void checkOut_SkipsCertificate_WhenAlreadyExists() {
        // Arrange
        testAttendance.checkIn();

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(testAttendance);
        when(certificateService.certificateExists(1L)).thenReturn(true);

        // Act
        attendanceService.checkOut(checkOutRequest);

        // Assert
        verify(certificateService).certificateExists(1L);
        verify(certificateService, never()).generateCertificate(any());
    }

    @Test
    @DisplayName("Check-out - Continues even if certificate generation fails")
    void checkOut_ContinuesOnCertificateError() {
        // Arrange
        testAttendance.checkIn();

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));
        when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));
        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(testAttendance);
        when(certificateService.certificateExists(1L)).thenReturn(false);
        doThrow(new RuntimeException("Certificate service error"))
                .when(certificateService).generateCertificate(any(Enrollment.class));

        // Act - Should not throw exception
        AttendanceResponseDto result = attendanceService.checkOut(checkOutRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(certificateService).generateCertificate(testEnrollment);
    }

    @Test
    @DisplayName("Check-out - Fails with invalid QR code")
    void checkOut_InvalidQrCode_ThrowsException() {
        // Arrange
        when(studentRepository.findByQrCodeData("INVALID-QR"))
                .thenReturn(Optional.empty());

        checkOutRequest.setQrCodeData("INVALID-QR");

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkOut(checkOutRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_QR_CODE);

        verify(studentRepository).findByQrCodeData("INVALID-QR");
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-out - Fails when not checked in")
    void checkOut_NotCheckedIn_ThrowsException() {
        // Arrange
        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkOut(checkOutRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHECKED_IN);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check-out - Fails when already checked out")
    void checkOut_AlreadyCheckedOut_ThrowsException() {
        // Arrange
        testAttendance.checkIn();
        testAttendance.checkOut(); // Already checked out

        when(studentRepository.findByQrCodeData("STUDENT-1-2012345"))
                .thenReturn(Optional.of(testStudent));
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(attendanceRepository.findByStudentIdAndActivityIdAndDate(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.checkOut(checkOutRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CHECKED_OUT);
    }

    // ==================== GET ACTIVITY ATTENDANCE TESTS ====================

    @Test
    @DisplayName("Get Activity Attendance - Success returns list")
    void getActivityAttendance_Success_ReturnsList() {
        // Arrange
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findByActivityId(1L))
                .thenReturn(List.of(testAttendance));

        // Act
        List<AttendanceResponseDto> result = attendanceService.getActivityAttendance(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(1L);

        verify(activityRepository).existsById(1L);
        verify(attendanceRepository).findByActivityId(1L);
    }

    @Test
    @DisplayName("Get Activity Attendance - Fails when activity not found")
    void getActivityAttendance_ActivityNotFound_ThrowsException() {
        // Arrange
        when(activityRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getActivityAttendance(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).existsById(999L);
        verify(attendanceRepository, never()).findByActivityId(anyLong());
    }

    @Test
    @DisplayName("Get Activity Attendance - Returns empty list when no attendances")
    void getActivityAttendance_ReturnsEmpty_WhenNoAttendances() {
        // Arrange
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findByActivityId(1L))
                .thenReturn(List.of());

        // Act
        List<AttendanceResponseDto> result = attendanceService.getActivityAttendance(1L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== GET ATTENDANCE SUMMARY TESTS ====================

    @Test
    @DisplayName("Get Attendance Summary - Success returns summary")
    void getAttendanceSummary_Success_ReturnsSummary() {
        // Arrange
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countByActivityAndStatus(testActivity, EnrollmentStatus.APPROVED))
                .thenReturn(10L);
        when(attendanceRepository.countPresentByActivityId(1L)).thenReturn(8L);
        when(attendanceRepository.countAbsentByActivityId(1L)).thenReturn(2L);
        when(attendanceRepository.countCheckedInByActivityId(1L)).thenReturn(8L);
        when(attendanceRepository.countCheckedOutByActivityId(1L)).thenReturn(5L);

        // Act
        AttendanceSummaryDto result = attendanceService.getAttendanceSummary(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getActivityId()).isEqualTo(1L);
        assertThat(result.getTotalEnrolled()).isEqualTo(10L);
        assertThat(result.getTotalPresent()).isEqualTo(8L);
        assertThat(result.getTotalAbsent()).isEqualTo(2L);
        assertThat(result.getTotalCheckedIn()).isEqualTo(8L);
        assertThat(result.getTotalCheckedOut()).isEqualTo(5L);
        assertThat(result.getAttendanceRate()).isEqualTo(80.0);

        verify(activityRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Attendance Summary - Handles zero enrollment")
    void getAttendanceSummary_HandlesZeroEnrollment() {
        // Arrange
        when(activityRepository.findById(1L))
                .thenReturn(Optional.of(testActivity));
        when(enrollmentRepository.countByActivityAndStatus(testActivity, EnrollmentStatus.APPROVED))
                .thenReturn(0L);
        when(attendanceRepository.countPresentByActivityId(1L)).thenReturn(0L);
        when(attendanceRepository.countAbsentByActivityId(1L)).thenReturn(0L);
        when(attendanceRepository.countCheckedInByActivityId(1L)).thenReturn(0L);
        when(attendanceRepository.countCheckedOutByActivityId(1L)).thenReturn(0L);

        // Act
        AttendanceSummaryDto result = attendanceService.getAttendanceSummary(1L);

        // Assert
        assertThat(result.getAttendanceRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Get Attendance Summary - Fails when activity not found")
    void getAttendanceSummary_ActivityNotFound_ThrowsException() {
        // Arrange
        when(activityRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getAttendanceSummary(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findById(999L);
    }

    // ==================== GET STUDENT ATTENDANCE HISTORY TESTS ====================

    @Test
    @DisplayName("Get Student Attendance History - Success returns history")
    void getStudentAttendanceHistory_Success_ReturnsHistory() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findByStudentId(1L))
                .thenReturn(List.of(testAttendance));

        // Act
        List<AttendanceResponseDto> result = attendanceService.getStudentAttendanceHistory(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(1L);
        assertThat(result.get(0).getActivityId()).isEqualTo(1L);

        verify(studentRepository).existsById(1L);
        verify(attendanceRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Get Student Attendance History - Fails when student not found")
    void getStudentAttendanceHistory_StudentNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getStudentAttendanceHistory(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).existsById(999L);
        verify(attendanceRepository, never()).findByStudentId(anyLong());
    }

    // ==================== GET STUDENT ACTIVITY ATTENDANCE TESTS ====================

    @Test
    @DisplayName("Get Student Activity Attendance - Success returns attendance")
    void getStudentActivityAttendance_Success_ReturnsAttendance() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.of(testAttendance));

        // Act
        AttendanceResponseDto result = attendanceService.getStudentActivityAttendance(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(1L);
        assertThat(result.getActivityId()).isEqualTo(1L);

        verify(attendanceRepository).findByStudentIdAndActivityId(1L, 1L);
    }

    @Test
    @DisplayName("Get Student Activity Attendance - Fails when student not found")
    void getStudentActivityAttendance_StudentNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getStudentActivityAttendance(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).existsById(999L);
    }

    @Test
    @DisplayName("Get Student Activity Attendance - Fails when activity not found")
    void getStudentActivityAttendance_ActivityNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(activityRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getStudentActivityAttendance(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).existsById(999L);
    }

    @Test
    @DisplayName("Get Student Activity Attendance - Fails when attendance not found")
    void getStudentActivityAttendance_AttendanceNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(activityRepository.existsById(1L)).thenReturn(true);
        when(attendanceRepository.findByStudentIdAndActivityId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.getStudentActivityAttendance(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ATTENDANCE_NOT_FOUND);

        verify(attendanceRepository).findByStudentIdAndActivityId(1L, 1L);
    }
}
