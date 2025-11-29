package com.ctxh.volunteer.module.activity.service;

import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface ActivityService {

    /**
     * Create a new activity
     */
    ActivityResponseDto createActivity(Long organizationId, @Valid CreateActivityRequestDto requestDto);

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
    ActivityResponseDto updateActivity(Long organizationId, Long activityId, @Valid UpdateActivityRequestDto requestDto);

    /**
     * Delete activity
     */
    void deleteActivity(Long organizationId, Long activityId);

    /**
     * Close activity registration
     */
    ActivityResponseDto closeActivityRegistration(Long organizationId, Long activityId);
}
