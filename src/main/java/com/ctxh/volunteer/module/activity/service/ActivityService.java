package com.ctxh.volunteer.module.activity.service;

import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ActivityService {

    /**
     * Create a new activity
     */
    ActivityResponseDto createActivity(CreateActivityRequestDto requestDto, MultipartFile imageFile);

    /**
     * Get all activities of an organization
     */
    List<ActivityListResponseDto> getActivitiesByOrganization(Long organizationId);

    /**
     * Get activity by ID
     */
    ActivityResponseDto getActivityById(Long activityId);

    /**
     * Update activity
     */
    ActivityResponseDto updateActivity(Long organizationId, Long activityId, UpdateActivityRequestDto requestDto);

    /**
     * Delete activity
     */
    void deleteActivity(Long organizationId, Long activityId);

    /**
     * Close activity registration
     */
    ActivityResponseDto closeActivityRegistration(Long organizationId, Long activityId);

    // ============ ENROLLMENT MANAGEMENT ============

    /**
     * Get all enrollments for an activity
     */
    List<EnrollmentResponseDto> getActivityEnrollments(Long activityId);

    /**
     * Get pending enrollments for an activity
     */
    List<EnrollmentResponseDto> getPendingEnrollments(Long activityId);

    /**
     * Get approved enrollments for an activity
     */
    List<EnrollmentResponseDto> getApprovedEnrollments(Long activityId);

    /**
     * Get rejected enrollments for an activity
     */
    List<EnrollmentResponseDto> getRejectedEnrollments(Long activityId);

    /**
     * Approve an enrollment
     */
    EnrollmentResponseDto approveEnrollment(Long activityId, Long enrollmentId, Long approvedByUserId);

    /**
     * Reject an enrollment
     */
    EnrollmentResponseDto rejectEnrollment(Long activityId, Long enrollmentId, Long rejectedByUserId);

    // ============ STUDENT DISCOVERY APIs ============

    /**
     * Get all available activities (OPEN status, not past registration deadline)
     */
    List<ActivityListResponseDto> getAllActivity();

    /**
     * Simple search activities by keyword
     */
    List<ActivityListResponseDto> searchActivities(String keyword);

    /**
     * Get activity detail (similar to getActivityById but for students)
     */
    ActivityResponseDto getActivityDetail(Long activityId);

    List<ActivityListResponseDto> searchActivitiesAdvanced(
            String keyword,
            ActivityCategory category,
            RegistrationState status,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Advanced search with filters
     */
}
