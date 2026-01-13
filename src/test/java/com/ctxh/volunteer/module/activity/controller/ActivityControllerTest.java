package com.ctxh.volunteer.module.activity.controller;

import com.ctxh.volunteer.common.exception.GlobalExceptionHandler;
import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import com.ctxh.volunteer.module.activity.service.ActivitySecurity;
import com.ctxh.volunteer.module.activity.service.ActivityService;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {ActivityController.class, GlobalExceptionHandler.class},
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
@DisplayName("ActivityController Integration Tests")
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityService activityService;

    @MockBean(name = "activitySecurity")
    private ActivitySecurity activitySecurity;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    private ActivityResponseDto activityResponseDto;
    private ActivityListResponseDto activityListResponseDto;
    private EnrollmentResponseDto enrollmentResponseDto;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1"); // Organization ID 1
        SecurityContextHolder.setContext(securityContext);

        // Mock ActivitySecurity to allow access
        when(activitySecurity.isOwner(anyLong())).thenReturn(true);

        activityResponseDto = ActivityResponseDto.builder()
                .activityId(1L)
                .name("Test Activity")
                .build();

        activityListResponseDto = ActivityListResponseDto.builder()
                .activityId(1L)
                .title("Test Activity")
                .build();

        enrollmentResponseDto = EnrollmentResponseDto.builder()
                .enrollmentId(1L)
                .studentId(1L)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/activities - Create activity successfully")
    void createActivity_Success_ReturnsCreated() throws Exception {
        CreateActivityRequestDto requestDto = new CreateActivityRequestDto();
        requestDto.setTitle("New Activity");
        requestDto.setStartDateTime(LocalDateTime.now().plusDays(1));
        requestDto.setEndDateTime(LocalDateTime.now().plusDays(2));
        requestDto.setMaxParticipants(10);
        requestDto.setTheNumberOfCtxhDay(1.0);
        requestDto.setAddress("Test Address");

        MockMultipartFile jsonFile = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(requestDto));
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image data".getBytes());

        when(activityService.createActivity(any(CreateActivityRequestDto.class), any())).thenReturn(activityResponseDto);

        mockMvc.perform(multipart("/api/v1/activities")
                        .file(jsonFile)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/activities/get-all-activity-for-organization - Success")
    void getAllActivitiesByOrganization_Success_ReturnsList() throws Exception {
        when(activityService.getActivitiesByOrganization(anyLong())).thenReturn(List.of(activityListResponseDto));

        mockMvc.perform(get("/api/v1/activities/get-all-activity-for-organization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId} - Success")
    void getActivityById_Success_ReturnsActivity() throws Exception {
        when(activityService.getActivityById(1L)).thenReturn(activityResponseDto);

        mockMvc.perform(get("/api/v1/activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activityId").value(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/activities/{activityId} - Update activity successfully")
    void updateActivity_Success_ReturnsUpdatedActivity() throws Exception {
        UpdateActivityRequestDto requestDto = new UpdateActivityRequestDto();
        requestDto.setName("Updated Activity");
        requestDto.setStartDateTime(LocalDateTime.now().plusDays(1));
        requestDto.setEndDateTime(LocalDateTime.now().plusDays(2));
        requestDto.setMaxParticipants(20);
        requestDto.setTheNumberOfCtxhDay(2.0);
        requestDto.setAddress("Updated Address");

        MockMultipartFile jsonFile = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(requestDto));

        when(activityService.updateActivity(anyLong(), anyLong(), any(UpdateActivityRequestDto.class), any())).thenReturn(activityResponseDto);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/activities/1")
                        .file(jsonFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/activities/{activityId} - Success")
    void deleteActivity_Success_ReturnsOk() throws Exception {
        doNothing().when(activityService).deleteActivity(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/activities/{activityId}/close - Success")
    void closeActivityRegistration_Success_ReturnsOk() throws Exception {
        when(activityService.closeActivityRegistration(anyLong(), anyLong())).thenReturn(activityResponseDto);

        mockMvc.perform(put("/api/v1/activities/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/activities/{activityId}/enrollments - Success")
    void getActivityEnrollments_Success_ReturnsList() throws Exception {
        when(activityService.getActivityEnrollments(1L)).thenReturn(List.of(enrollmentResponseDto));

        mockMvc.perform(get("/api/v1/activities/1/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/activities/{activityId}/enrollments/{enrollmentId}/approve - Success")
    void approveEnrollment_Success_ReturnsApproved() throws Exception {
        when(activityService.approveEnrollment(anyLong(), anyLong(), anyLong())).thenReturn(enrollmentResponseDto);

        mockMvc.perform(put("/api/v1/activities/1/enrollments/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/activities/{activityId}/enrollments/{enrollmentId}/reject - Success")
    void rejectEnrollment_Success_ReturnsRejected() throws Exception {
        when(activityService.rejectEnrollment(anyLong(), anyLong(), anyLong())).thenReturn(enrollmentResponseDto);

        mockMvc.perform(put("/api/v1/activities/1/enrollments/1/reject")
                        .param("rejectedBy", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/activities - Browse activities")
    void getAllActivities_Success_ReturnsList() throws Exception {
        when(activityService.getAllActivity()).thenReturn(List.of(activityListResponseDto));

        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/activities/search - Success")
    void searchActivities_Success_ReturnsList() throws Exception {
        when(activityService.searchActivities(anyString())).thenReturn(List.of(activityListResponseDto));

        mockMvc.perform(get("/api/v1/activities/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/activities/searchAdvanced - Success")
    void searchActivitiesAdvanced_Success_ReturnsList() throws Exception {
        when(activityService.searchActivitiesAdvanced(any(), any(), any(), any(), any()))
                .thenReturn(List.of(activityListResponseDto));

        mockMvc.perform(get("/api/v1/activities/searchAdvanced")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
