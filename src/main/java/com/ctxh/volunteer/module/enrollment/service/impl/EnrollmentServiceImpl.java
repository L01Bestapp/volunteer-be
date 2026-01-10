package com.ctxh.volunteer.module.enrollment.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentRequestDto;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.ctxh.volunteer.module.enrollment.dto.MyActivityResponseDto;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.enrollment.service.EnrollmentService;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ActivityRepository activityRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public EnrollmentResponseDto enrollInActivity(Long studentId, @Valid EnrollmentRequestDto requestDto) {
        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Find activity
        Activity activity = activityRepository.findById(requestDto.getActivityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Check if a student already enrolled
        enrollmentRepository.findByStudentIdAndActivityId(studentId, requestDto.getActivityId())
                .ifPresent(enrollment -> {
                    throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
                });

        // Check if activity can accept registration - USE HELPER METHOD
        if (!activity.canRegister()) {
            throw new BusinessException(ErrorCode.ACTIVITY_MAX_PENDING_REACHED);
        }

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .activity(activity)
                .status(EnrollmentStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .enrollmentDate(LocalDateTime.now())
                .build();

        // Use helper methods to update relationships and counts
        student.addEnrollment(enrollment);
        activity.addEnrollment(enrollment);
        activity.incrementPending(); // USE HELPER METHOD

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Student {} enrolled in activity {}", studentId, requestDto.getActivityId());

        return mapToEnrollmentResponseDto(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyActivityResponseDto> getMyRequests(Long studentId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(this::mapToMyActivityResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyActivityResponseDto> getMyActivities(Long studentId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(
                studentId,
                EnrollmentStatus.APPROVED
        );

        return enrollments.stream()
                .map(this::mapToMyActivityResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long studentId, Long enrollmentId) {
        // Find enrollment and verify ownership
        Enrollment enrollment = enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Check if enrollment can be cancelled - USE HELPER METHOD
        if (!enrollment.canBeCancelled()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
        }

        Activity activity = enrollment.getActivity();
        Student student = enrollment.getStudent();

        // Decrement pending count using helper method
        activity.decrementPending();
        // Remove enrollment using helper methods
        student.removeEnrollment(enrollment);
        activity.removeEnrollment(enrollment);

        enrollmentRepository.delete(enrollment);
        log.info("Student {} cancelled enrollment {}", studentId, enrollmentId);
    }

    // ============ MAPPING METHODS ============

    private EnrollmentResponseDto mapToEnrollmentResponseDto(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .status(enrollment.getStatus())
                .appliedAt(enrollment.getAppliedAt())
                .approvedAt(enrollment.getApprovedAt())
                .approvedBy(enrollment.getApprovedBy())
                .rejectedAt(enrollment.getRejectedAt())
                .rejectedBy(enrollment.getRejectedBy())
                .isCompleted(enrollment.getIsCompleted())
                .completedAt(enrollment.getCompletedAt())
                .studentId(enrollment.getStudent().getStudentId())
                .fullName(enrollment.getStudent().getFullName())
                .mssv(enrollment.getStudent().getMssv())
                .email(enrollment.getStudent().getUser().getEmail())
                .phoneNumber(enrollment.getStudent().getPhoneNumber())
                .academicYear(enrollment.getStudent().getAcademicYear())
                .faculty(enrollment.getStudent().getFaculty())
                .gender(enrollment.getStudent().getGender())
                .dateOfBirth(enrollment.getStudent().getDateOfBirth())
                .totalCtxhDays(enrollment.getStudent().getTotalCtxhDays())
                .build();
    }

    private MyActivityResponseDto mapToMyActivityResponseDto(Enrollment enrollment) {
        Activity activity = enrollment.getActivity();

        return MyActivityResponseDto.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .enrollmentStatus(enrollment.getStatus())
                .appliedAt(enrollment.getAppliedAt())
                .approvedAt(enrollment.getApprovedAt())
                .isCompleted(enrollment.getIsCompleted())
                .completedAt(enrollment.getCompletedAt())
                .activityId(activity.getActivityId())
                .activityTitle(activity.getTitle())
                .shortDescription(activity.getShortDescription())
                .category(activity.getCategory())
                .registrationState(activity.getRegistrationState())
                .startDateTime(activity.getStartDateTime())
                .endDateTime(activity.getEndDateTime())
                .address(activity.getAddress())
                .benefitsCtxh(activity.getTheNumberOfCtxhDay())
                .organizationId(activity.getOrganization().getOrganizationId())
                .organizationName(activity.getOrganization().getOrganizationName())
                .build();
    }
}
