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
import com.ctxh.volunteer.module.attendance.repository.AttendanceRepository;
import com.ctxh.volunteer.module.attendance.service.AttendanceService;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public AttendanceResponseDto checkIn(@Valid QrCheckInRequestDto requestDto) {
        // Find student by QR code
        Student student = studentRepository.findByQrCodeData(requestDto.getQrCodeData())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_QR_CODE));

        // Find activity
        Activity activity = activityRepository.findById(requestDto.getActivityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Verify student is enrolled and approved
        enrollmentRepository.findByStudentIdAndActivityId(student.getStudentId(), activity.getActivityId())
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.APPROVED)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED));

        // Find or create an attendance record for today
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Attendance attendance = attendanceRepository
                .findByStudentIdAndActivityIdAndDate(
                        student.getStudentId(),
                        activity.getActivityId(),
                        today,
                        endOfDay
                )
                .orElseGet(() -> {
                    // Create a new attendance record
                    Attendance newAttendance = Attendance.builder()
                            .student(student)
                            .activity(activity)
                            .attendanceDate(LocalDateTime.now())
                            .build();
                    student.addAttendance(newAttendance);
                    activity.getAttendances().add(newAttendance);
                    return newAttendance;
                });

        // Check-in using a helper method
        try {
            attendance.checkIn(); // USE HELPER METHOD - sets checkInTime and status to PRESENT
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Student {} checked in to activity {}", student.getStudentId(), activity.getActivityId());

        return mapToAttendanceResponseDto(savedAttendance);
    }

    @Override
    @Transactional
    public AttendanceResponseDto checkOut(@Valid QrCheckOutRequestDto requestDto) {
        // Find student by QR code
        Student student = studentRepository.findByQrCodeData(requestDto.getQrCodeData())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_QR_CODE));

        // Find activity
        Activity activity = activityRepository.findById(requestDto.getActivityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Find attendance record for today
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Attendance attendance = attendanceRepository
                .findByStudentIdAndActivityIdAndDate(
                        student.getStudentId(),
                        activity.getActivityId(),
                        today,
                        endOfDay
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHECKED_IN));

        // Check-out using helper method
        try {
            attendance.checkOut(); // USE HELPER METHOD - sets checkOutTime
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndActivityId(student.getStudentId(), activity.getActivityId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED));

            // Update a student's CTXH days based on activity's CTXH hours
            enrollment.complete();

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("already checked out")) {
                throw new BusinessException(ErrorCode.ALREADY_CHECKED_OUT);
            } else {
                throw new BusinessException(ErrorCode.NOT_CHECKED_IN);
            }
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Student {} checked out from activity {}", student.getStudentId(), activity.getActivityId());

        return mapToAttendanceResponseDto(savedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getActivityAttendance(Long activityId) {
        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<Attendance> attendances = attendanceRepository.findByActivityId(activityId);
        return attendances.stream()
                .map(this::mapToAttendanceResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryDto getAttendanceSummary(Long activityId) {
        // Find activity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Get enrollment count (total approved students)
        Long totalEnrolled = enrollmentRepository.countByActivityAndStatus(
                activity,
                EnrollmentStatus.APPROVED
        );

        // Get attendance statistics
        Long totalPresent = attendanceRepository.countPresentByActivityId(activityId);
        Long totalAbsent = attendanceRepository.countAbsentByActivityId(activityId);
        Long totalCheckedIn = attendanceRepository.countCheckedInByActivityId(activityId);
        Long totalCheckedOut = attendanceRepository.countCheckedOutByActivityId(activityId);

        // Calculate attendance rate
        double attendanceRate = 0.0;
        if (totalEnrolled > 0) {
            attendanceRate = (totalPresent.doubleValue() / totalEnrolled.doubleValue()) * 100.0;
        }

        return AttendanceSummaryDto.builder()
                .activityId(activity.getActivityId())
                .activityTitle(activity.getTitle())
                .totalEnrolled(totalEnrolled)
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .totalCheckedIn(totalCheckedIn)
                .totalCheckedOut(totalCheckedOut)
                .attendanceRate(Math.round(attendanceRate * 100.0) / 100.0) // Round to 2 decimal places
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getStudentAttendanceHistory(Long studentId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        return attendances.stream()
                .map(this::mapToAttendanceResponseDtoWithActivity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDto getStudentActivityAttendance(Long studentId, Long activityId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        Attendance attendance = attendanceRepository
                .findByStudentIdAndActivityId(studentId, activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        return mapToAttendanceResponseDtoWithActivity(attendance);
    }

    // ============ MAPPING METHODS ============

    private AttendanceResponseDto mapToAttendanceResponseDto(Attendance attendance) {
        return AttendanceResponseDto.builder()
                .attendanceId(attendance.getAttendanceId())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .durationMinutes(attendance.getAttendanceDurationMinutes()) // USE HELPER METHOD
                .studentId(attendance.getStudent().getStudentId())
                .fullName(attendance.getStudent().getFullName())
                .mssv(attendance.getStudent().getMssv())
                .email(attendance.getStudent().getUser().getEmail())
                .phoneNumber(attendance.getStudent().getPhoneNumber())
                .academicYear(attendance.getStudent().getAcademicYear())
                .faculty(attendance.getStudent().getFaculty())
                .gender(attendance.getStudent().getGender())
                .dateOfBirth(attendance.getStudent().getDateOfBirth())
                .build();
    }

    private AttendanceResponseDto mapToAttendanceResponseDtoWithActivity(Attendance attendance) {
        return AttendanceResponseDto.builder()
                .attendanceId(attendance.getAttendanceId())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .durationMinutes(attendance.getAttendanceDurationMinutes()) // USE HELPER METHOD
                .studentId(attendance.getStudent().getStudentId())
                .fullName(attendance.getStudent().getFullName())
                .mssv(attendance.getStudent().getMssv())
                .email(attendance.getStudent().getUser().getEmail())
                .phoneNumber(attendance.getStudent().getPhoneNumber())
                .academicYear(attendance.getStudent().getAcademicYear())
                .faculty(attendance.getStudent().getFaculty())
                .gender(attendance.getStudent().getGender())
                .dateOfBirth(attendance.getStudent().getDateOfBirth())
                .activityId(attendance.getActivity().getActivityId())
                .activityTitle(attendance.getActivity().getTitle())
                .organizationName(attendance.getActivity().getOrganization().getOrganizationName())
                .build();
    }
}
