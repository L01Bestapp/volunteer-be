package com.ctxh.volunteer.module.activity.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.activity.service.ActivityService;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    @Operation(summary = "create a new activity", description = "Request body for creating a new activity")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ActivityResponseDto> createActivity(
            @RequestParam("organizationId") Long organizationId,
            @Valid @RequestPart("data") CreateActivityRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
            ) {
        return ApiResponse.ok(
                "Activity created successfully",
                activityService.createActivity(organizationId, requestDto, imageFile)
        );
    }

    /**
     * Get all activities of an organization
     * GET /api/v1/activities?organizationId={organizationId}
     */
    @Operation(summary = "get all activities of an organization")
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
    @Operation(summary = "get activity by ID")
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
    @Operation(summary = "update activity")
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
    @Operation(summary = "delete activity")
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
    @Operation(summary = "close activity registration")
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
    @Operation(summary = "get all enrollments for an activity")
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
    @Operation(summary = "get pending enrollments for an activity")
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
    @Operation(summary = "get approved enrollments for an activity")
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
    @Operation(summary = "get rejected enrollments for an activity")
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
    @Operation(summary = "approve an enrollment")
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
    @Operation(summary = "reject an enrollment")
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

    // ============ STUDENT DISCOVERY APIs ============

    /**
     * Browse available activities
     * GET /api/v1/activities/available
     */
    @Operation(summary = "get all available activities for students")
    @GetMapping("/available")
    public ApiResponse<List<ActivityListResponseDto>> getAvailableActivities() {
        return ApiResponse.ok(
                "Available activities retrieved successfully",
                activityService.getAvailableActivities()
        );
    }

    /**
     * Simple search activities
     * GET /api/v1/activities/search?keyword=...
     */
    @Operation(summary = "search activities by keyword")
    @GetMapping("/search")
    public ApiResponse<List<ActivityListResponseDto>> searchActivities(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.ok(
                "Activities search completed successfully",
                activityService.searchActivities(keyword)
        );
    }

    /**
     * Get activity detail
     * GET /api/v1/activities/{activityId}/detail
     */
    @Operation(summary = "get activity detail for students")
    @GetMapping("/{activityId}/detail")
    public ApiResponse<ActivityResponseDto> getActivityDetail(
            @PathVariable("activityId") Long activityId) {
        return ApiResponse.ok(
                "Activity detail retrieved successfully",
                activityService.getActivityDetail(activityId)
        );
    }

    /**
     * Advanced search activities
     * GET /api/v1/activities/searchAdvanced?keyword=...&category=...&status=...&startDate=...&endDate=...
     */
    @Operation(summary = "advanced search activities with filters")
    @GetMapping("/searchAdvanced")
    public ApiResponse<List<ActivityListResponseDto>> searchActivitiesAdvanced(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) ActivityCategory category,
            @RequestParam(value = "status", required = false) ActivityStatus status,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {


        return ApiResponse.ok(
                "Advanced search completed successfully",
                activityService.searchActivitiesAdvanced(keyword, category, status, startDate, endDate)
        );
    }
}
