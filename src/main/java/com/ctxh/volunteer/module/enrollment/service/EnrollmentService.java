package com.ctxh.volunteer.module.enrollment.service;

import com.ctxh.volunteer.module.enrollment.dto.EnrollmentRequestDto;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.ctxh.volunteer.module.enrollment.dto.MyActivityResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface EnrollmentService {

    /**
     * Enroll in an activity
     */
    EnrollmentResponseDto enrollInActivity(Long studentId, @Valid EnrollmentRequestDto requestDto);

    /**
     * Get all enrollment requests by student (all statuses)
     */
    List<EnrollmentResponseDto> getMyRequests(Long studentId);

    /**
     * Get approved activities for student
     */
    List<MyActivityResponseDto> getMyActivities(Long studentId);

    /**
     * Cancel enrollment
     */
    void cancelEnrollment(Long studentId, Long enrollmentId);
}
