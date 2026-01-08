package com.ctxh.volunteer.module.organization.controller;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import com.ctxh.volunteer.module.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
@DisplayName("OrganizationController Integration Tests")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private CreateOrganizationRequestDto createRequest;
    private UpdateOrganizationRequestDto updateRequest;
    private OrganizationResponseDto organizationResponse;

    @BeforeEach
    void setUp() {
        // Setup create request
        createRequest = new CreateOrganizationRequestDto();
        createRequest.setEmail("org@example.com");
        createRequest.setPassword("password123");
        createRequest.setOrganizationName("Test Organization");
        createRequest.setOrganizationType("NGO");

        // Setup update request
        updateRequest = new UpdateOrganizationRequestDto();
        updateRequest.setRepresentativeName("Jane Smith");
        updateRequest.setRepresentativeEmail("jane@test.com");
        updateRequest.setRepresentativePhoneNumber("0987654321");
        updateRequest.setBio("Updated bio");

        // Setup response
        organizationResponse = OrganizationResponseDto.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .email("org@example.com")
                .type(OrganizationType.NGO)
                .representativeName("John Doe")
                .representativeEmail("john@test.com")
                .representativePhoneNumber("0123456789")
                .avatarUrl("default-avatar.png")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE ORGANIZATION TESTS ====================

    @Test
    @DisplayName("POST /register - Success creates organization")
    void createOrganization_Success_ReturnsCreated() throws Exception {
        // Arrange
        when(organizationService.registerOrganization(any(CreateOrganizationRequestDto.class)))
                .thenReturn(organizationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Organization created successfully"))
                .andExpect(jsonPath("$.data.organizationId").value(1L))
                .andExpect(jsonPath("$.data.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.data.email").value("org@example.com"))
                .andExpect(jsonPath("$.data.type").value("NGO"));
    }

    @Test
    @DisplayName("POST /register - Fails with invalid email")
    void createOrganization_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Fails when organization name already exists")
    void createOrganization_NameExists_ReturnsConflict() throws Exception {
        // Arrange
        when(organizationService.registerOrganization(any(CreateOrganizationRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.ORGANIZATION_NAME_ALREADY_EXISTS));

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /register - Fails with missing required fields")
    void createOrganization_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateOrganizationRequestDto invalidRequest = new CreateOrganizationRequestDto();

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Fails with short password")
    void createOrganization_ShortPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setPassword("123"); // Too short

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Accepts different organization types")
    void createOrganization_DifferentTypes_AcceptsAll() throws Exception {
        // Arrange
        createRequest.setOrganizationType("GOVERNMENT");
        organizationResponse.setType(OrganizationType.GOVERNMENT);

        when(organizationService.registerOrganization(any(CreateOrganizationRequestDto.class)))
                .thenReturn(organizationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("GOVERNMENT"));
    }

    // ==================== UPDATE ORGANIZATION TESTS ====================

    @Test
    @DisplayName("PUT /{id} - Success updates organization")
    void updateOrganization_Success_ReturnsOk() throws Exception {
        // Arrange
        OrganizationResponseDto updatedResponse = OrganizationResponseDto.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .email("org@example.com")
                .type(OrganizationType.NGO)
                .representativeName("Jane Smith")
                .representativeEmail("jane@test.com")
                .representativePhoneNumber("0987654321")
                .bio("Updated bio")
                .build();

        when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequestDto.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/organization/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizationId").value(1L))
                .andExpect(jsonPath("$.data.representativeName").value("Jane Smith"))
                .andExpect(jsonPath("$.data.representativeEmail").value("jane@test.com"))
                .andExpect(jsonPath("$.data.representativePhoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.bio").value("Updated bio"));
    }

    @Test
    @DisplayName("PUT /{id} - Fails when organization not found")
    void updateOrganization_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(organizationService.updateOrganization(eq(999L), any(UpdateOrganizationRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(put("/api/v1/organization/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /{id} - Accepts partial updates")
    void updateOrganization_PartialUpdate_Works() throws Exception {
        // Arrange
        UpdateOrganizationRequestDto partialUpdate = new UpdateOrganizationRequestDto();
        partialUpdate.setRepresentativeName("New Name");

        OrganizationResponseDto updatedResponse = OrganizationResponseDto.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .email("org@example.com")
                .type(OrganizationType.NGO)
                .representativeName("New Name")
                .representativeEmail("john@test.com") // Unchanged
                .build();

        when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequestDto.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/organization/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.representativeName").value("New Name"));
    }

    @Test
    @DisplayName("PUT /{id} - Accepts empty update request")
    void updateOrganization_EmptyRequest_Works() throws Exception {
        // Arrange
        UpdateOrganizationRequestDto emptyUpdate = new UpdateOrganizationRequestDto();

        when(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequestDto.class)))
                .thenReturn(organizationResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/organization/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== GET ORGANIZATION BY ID TESTS ====================

    @Test
    @DisplayName("GET /{id} - Success returns organization")
    void getOrganizationById_Success_ReturnsOrganization() throws Exception {
        // Arrange
        when(organizationService.getOrganizationById(1L))
                .thenReturn(organizationResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Organization retrieved successfully"))
                .andExpect(jsonPath("$.data.organizationId").value(1L))
                .andExpect(jsonPath("$.data.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.data.email").value("org@example.com"))
                .andExpect(jsonPath("$.data.type").value("NGO"));
    }

    @Test
    @DisplayName("GET /{id} - Fails when organization not found")
    void getOrganizationById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(organizationService.getOrganizationById(999L))
                .thenThrow(new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/organization/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /{id} - Returns all organization details")
    void getOrganizationById_ReturnsAllDetails() throws Exception {
        // Arrange
        when(organizationService.getOrganizationById(1L))
                .thenReturn(organizationResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(1L))
                .andExpect(jsonPath("$.data.organizationName").value("Test Organization"))
                .andExpect(jsonPath("$.data.email").value("org@example.com"))
                .andExpect(jsonPath("$.data.type").value("NGO"))
                .andExpect(jsonPath("$.data.representativeName").value("John Doe"))
                .andExpect(jsonPath("$.data.representativeEmail").value("john@test.com"))
                .andExpect(jsonPath("$.data.representativePhoneNumber").value("0123456789"))
                .andExpect(jsonPath("$.data.avatarUrl").value("default-avatar.png"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /register - Fails with invalid organization type")
    void createOrganization_InvalidType_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setOrganizationType("INVALID_TYPE");

        // Act & Assert
        mockMvc.perform(post("/api/v1/organization/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /{id} - Validates email format in update")
    void updateOrganization_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        updateRequest.setRepresentativeEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(put("/api/v1/organization/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}
