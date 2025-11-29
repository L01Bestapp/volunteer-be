package com.ctxh.volunteer.module.activity.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.service.ActivityService;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * Create a new activity
     * POST /api/v1/activities
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ActivityResponseDto> createActivity(
            @RequestParam("organizationId") Long organizationId,
            @Valid @RequestBody CreateActivityRequestDto requestDto) {
        return ApiResponse.ok(
                "Activity created successfully",
                activityService.createActivity(organizationId, requestDto)
        );
    }

    /**
     * Get all activities of an organization
     * GET /api/v1/activities?organizationId={organizationId}
     */
    @GetMapping
    public ApiResponse<List<ActivityListResponseDto>> getActivitiesByOrganization(
            @RequestParam("organizationId") Long organizationId) {
        return ApiResponse.ok(
                "Activities retrieved successfully",
                activityService.getActivitiesByOrganization(organizationId)
        );
    }

    /**
     * Get activity by ID
     * GET /api/v1/activities/{activityId}
     */
    @GetMapping("/{activityId}")
    public ApiResponse<ActivityResponseDto> getActivityById(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Activity retrieved successfully",
                activityService.getActivityById(activityId)
        );
    }

    /**
     * Update activity
     * PUT /api/v1/activities/{activityId}
     */
    @PutMapping("/{activityId}")
    public ApiResponse<ActivityResponseDto> updateActivity(
            @RequestParam("organizationId") Long organizationId,
            @PathVariable("activityId") Long activityId,
            @Valid @RequestBody UpdateActivityRequestDto requestDto) {
        return ApiResponse.ok(
                "Activity updated successfully",
                activityService.updateActivity(organizationId, activityId, requestDto)
        );
    }

    /**
     * Delete activity
     * DELETE /api/v1/activities/{activityId}
     */
    @DeleteMapping("/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteActivity(
            @RequestParam("organizationId") Long organizationId,
            @PathVariable("activityId") Long activityId) {
        activityService.deleteActivity(organizationId, activityId);
        return ApiResponse.ok("Activity deleted successfully", null);
    }

    /**
     * Close activity registration
     * PATCH /api/v1/activities/{activityId}/close
     */
    @PatchMapping("/{activityId}/close")
    public ApiResponse<ActivityResponseDto> closeActivityRegistration(
            @RequestParam("organizationId") Long organizationId,
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Activity registration closed successfully",
                activityService.closeActivityRegistration(organizationId, activityId)
        );
    }

    // ============ ENROLLMENT MANAGEMENT ============

    /**
     * Get all enrollments for an activity
     * GET /api/v1/activities/{activityId}/enrollments
     */
    @GetMapping("/{activityId}/enrollments")
    public ApiResponse<List<EnrollmentResponseDto>> getActivityEnrollments(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Enrollments retrieved successfully",
                activityService.getActivityEnrollments(activityId)
        );
    }

    /**
     * Get pending enrollments for an activity
     * GET /api/v1/activities/{activityId}/enrollments/pending
     */
    @GetMapping("/{activityId}/enrollments/pending")
    public ApiResponse<List<EnrollmentResponseDto>> getPendingEnrollments(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Pending enrollments retrieved successfully",
                activityService.getPendingEnrollments(activityId)
        );
    }

    /**
     * Get approved enrollments for an activity
     * GET /api/v1/activities/{activityId}/enrollments/approved
     */
    @GetMapping("/{activityId}/enrollments/approved")
    public ApiResponse<List<EnrollmentResponseDto>> getApprovedEnrollments(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Approved enrollments retrieved successfully",
                activityService.getApprovedEnrollments(activityId)
        );
    }

    /**
     * Get rejected enrollments for an activity
     * GET /api/v1/activities/{activityId}/enrollments/rejected
     */
    @GetMapping("/{activityId}/enrollments/rejected")
    public ApiResponse<List<EnrollmentResponseDto>> getRejectedEnrollments(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Rejected enrollments retrieved successfully",
                activityService.getRejectedEnrollments(activityId)
        );
    }

    /**
     * Approve an enrollment
     * PATCH /api/v1/activities/{activityId}/enrollments/{enrollmentId}/approve
     */
    @PatchMapping("/{activityId}/enrollments/{enrollmentId}/approve")
    public ApiResponse<EnrollmentResponseDto> approveEnrollment(
            @PathVariable("activityId") Long activityId,
            @PathVariable("enrollmentId") Long enrollmentId,
            @RequestParam("approvedBy") Long approvedByUserId) {
        return ApiResponse.ok(
                "Enrollment approved successfully",
                activityService.approveEnrollment(activityId, enrollmentId, approvedByUserId)
        );
    }

    /**
     * Reject an enrollment
     * PATCH /api/v1/activities/{activityId}/enrollments/{enrollmentId}/reject
     */
    @PatchMapping("/{activityId}/enrollments/{enrollmentId}/reject")
    public ApiResponse<EnrollmentResponseDto> rejectEnrollment(
            @PathVariable("activityId") Long activityId,
            @PathVariable("enrollmentId") Long enrollmentId,
            @RequestParam("rejectedBy") Long rejectedByUserId) {
        return ApiResponse.ok(
                "Enrollment rejected successfully",
                activityService.rejectEnrollment(activityId, enrollmentId, rejectedByUserId)
        );
    }
}
