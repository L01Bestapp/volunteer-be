package com.ctxh.volunteer.module.enrollment.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.common.util.AuthUtil;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentRequestDto;
import com.ctxh.volunteer.module.enrollment.dto.MyActivityResponseDto;
import com.ctxh.volunteer.module.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll in an activity
     * POST /api/v1/enrollments
     */
    @Operation(summary = "enroll in an activity")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> enrollInActivity(
            @Valid @RequestBody EnrollmentRequestDto requestDto) {
        Long studentId = AuthUtil.getIdFromAuthentication();
        enrollmentService.enrollInActivity(studentId, requestDto);
        return ApiResponse.ok(
                "Enrollment request submitted successfully"
        );
    }

    /**
     * Get all enrollment requests by student
     * GET /api/v1/enrollments/my-requests
     */
    @Operation(summary = "get all enrollment requests by student")
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<MyActivityResponseDto>> getMyRequests() {
        Long studentId = AuthUtil.getIdFromAuthentication();
        return ApiResponse.ok(
                "Enrollment requests retrieved successfully",
                enrollmentService.getMyRequests(studentId)
        );
    }

    /**
     * Get approved activities for student
     * GET /api/v1/enrollments/my-activities
     */
    @Operation(summary = "get approved activities for student")
    @GetMapping("/my-activities")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<MyActivityResponseDto>> getMyActivities() {
        Long studentId = AuthUtil.getIdFromAuthentication();
        return ApiResponse.ok(
                "My activities retrieved successfully",
                enrollmentService.getMyActivities(studentId)
        );
    }

    /**
     * Cancel enrollment
     * DELETE /api/v1/enrollments/{enrollmentId}
     */
    @Operation(summary = "cancel enrollment")
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> cancelEnrollment(
            @PathVariable("enrollmentId") Long enrollmentId) {
        Long studentId = AuthUtil.getIdFromAuthentication();
        enrollmentService.cancelEnrollment(studentId, enrollmentId);
        return ApiResponse.ok("Enrollment cancelled successfully", null);
    }
}
