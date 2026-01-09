package com.ctxh.volunteer.module.student.controller;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.certificate.dto.CertificateResponseDto;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.ParticipationHistoryDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {StudentController.class, com.ctxh.volunteer.common.exception.GlobalExceptionHandler.class},
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
@DisplayName("StudentController Integration Tests")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private CreateStudentRequestDto createRequest;
    private UpdateStudentRequestDto updateRequest;
    private StudentResponseDto studentResponse;

    @BeforeEach
    void setUp() {
        // Setup create request
        createRequest = new CreateStudentRequestDto();
        createRequest.setEmail("student@hcmut.edu.vn");
        createRequest.setPassword("password123");
        createRequest.setFullName("Nguyen Van An");
        createRequest.setMssv("2012345");
//        createRequest.setGender("MALE");

        // Setup update request
        updateRequest = new UpdateStudentRequestDto();
        updateRequest.setFullName("Updated Name");
        updateRequest.setPhoneNumber("84123456789");
        updateRequest.setFaculty("Computer Science");

        // Setup response
        studentResponse = StudentResponseDto.builder()
                .studentId(1L)
                .email("student@hcmut.edu.vn")
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .gender(Gender.MALE)
                .totalCtxhDays(0.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE STUDENT TESTS ====================

    @Test
    @DisplayName("POST /register - Success creates student")
    void registerStudent_Success_ReturnsCreated() throws Exception {
        // Arrange
        when(studentService.registerStudent(any(CreateStudentRequestDto.class)))
                .thenReturn(studentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student created successfully"))
                .andExpect(jsonPath("$.data.studentId").value(1L))
                .andExpect(jsonPath("$.data.email").value("student@hcmut.edu.vn"))
                .andExpect(jsonPath("$.data.mssv").value("2012345"));
    }

    @Test
    @DisplayName("POST /register - Fails with invalid email")
    void registerStudent_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Fails when MSSV already exists")
    void registerStudent_MssvExists_ReturnsConflict() throws Exception {
        // Arrange
        when(studentService.registerStudent(any(CreateStudentRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.MSSV_ALREADY_EXISTS));

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /register - Fails when email already registered")
    void registerStudent_EmailExists_ReturnsConflict() throws Exception {
        // Arrange
        when(studentService.registerStudent(any(CreateStudentRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED));

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== UPDATE STUDENT TESTS ====================

    @Test
    @DisplayName("PUT /{id} - Success updates student")
    void updateStudent_Success_ReturnsOk() throws Exception {
        // Arrange
        StudentResponseDto updatedResponse = StudentResponseDto.builder()
                .studentId(1L)
                .fullName("Updated Name")
                .phoneNumber("0123456789")
                .faculty("Computer Science")
                .build();

        when(studentService.updateStudent(eq(1L), any(UpdateStudentRequestDto.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student updated successfully"))
                .andExpect(jsonPath("$.data.studentId").value(1L))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0123456789"));
    }

    @Test
    @DisplayName("PUT /{id} - Fails when student not found")
    void updateStudent_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(studentService.updateStudent(eq(999L), any(UpdateStudentRequestDto.class)))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(put("/api/v1/students/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET STUDENT BY ID TESTS ====================

    @Test
    @DisplayName("GET /{id} - Success returns student")
    void getStudentById_Success_ReturnsStudent() throws Exception {
        // Arrange
        when(studentService.getStudentById(1L)).thenReturn(studentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(1L))
                .andExpect(jsonPath("$.data.mssv").value("2012345"));
    }

    @Test
    @DisplayName("GET /{id} - Fails when student not found")
    void getStudentById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(studentService.getStudentById(999L))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET STUDENT BY MSSV TESTS ====================

    @Test
    @DisplayName("GET /mssv/{mssv} - Success returns student")
    void getStudentByMssv_Success_ReturnsStudent() throws Exception {
        // Arrange
        when(studentService.getStudentByMssv("2012345")).thenReturn(studentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/mssv/2012345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student retrieved successfully"))
                .andExpect(jsonPath("$.data.mssv").value("2012345"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("GET /mssv/{mssv} - Fails when student not found")
    void getStudentByMssv_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(studentService.getStudentByMssv("9999999"))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/mssv/9999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET PARTICIPATION HISTORY TESTS ====================

    @Test
    @DisplayName("GET /participation/history - Success returns history")
    void getParticipationHistory_Success_ReturnsHistory() throws Exception {
        // Arrange
        ParticipationHistoryDto historyDto = ParticipationHistoryDto.builder()
                .enrollmentId(1L)
                .activityId(1L)
                .activityTitle("Test Activity")
                .enrollmentStatus(EnrollmentStatus.APPROVED)
                .organizationName("Test Org")
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .hasAttendance(true)
                .hasCertificate(true)
                .build();

        when(studentService.getParticipationHistory(1L))
                .thenReturn(List.of(historyDto));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/participation/history")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Participation history retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].enrollmentId").value(1L))
                .andExpect(jsonPath("$.data[0].activityTitle").value("Test Activity"))
                .andExpect(jsonPath("$.data[0].hasAttendance").value(true))
                .andExpect(jsonPath("$.data[0].hasCertificate").value(true));
    }

    @Test
    @DisplayName("GET /participation/history - Returns empty list when no history")
    void getParticipationHistory_NoHistory_ReturnsEmptyList() throws Exception {
        // Arrange
        when(studentService.getParticipationHistory(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/participation/history")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /participation/history - Fails when student not found")
    void getParticipationHistory_StudentNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(studentService.getParticipationHistory(999L))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/participation/history")
                        .param("studentId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET STUDENT CERTIFICATES TESTS ====================

    @Test
    @DisplayName("GET /certificates - Success returns certificates")
    void getStudentCertificates_Success_ReturnsCertificates() throws Exception {
        // Arrange
        CertificateResponseDto certificateDto = CertificateResponseDto.builder()
                .certificateId(1L)
                .certificateCode("CERT-001")
                .studentId(1L)
                .studentName("Nguyen Van A")
                .studentMssv("2012345")
                .activityId(1L)
                .activityTitle("Test Activity")
                .organizationName("Test Org")
                .issuedDate(LocalDateTime.now())
                .isRevoked(false)
                .build();

        when(studentService.getStudentCertificates(1L))
                .thenReturn(List.of(certificateDto));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/certificates")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Certificates retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].certificateId").value(1L))
                .andExpect(jsonPath("$.data[0].certificateCode").value("CERT-001"))
                .andExpect(jsonPath("$.data[0].studentMssv").value("2012345"))
                .andExpect(jsonPath("$.data[0].isRevoked").value(false));
    }

    @Test
    @DisplayName("GET /certificates - Returns empty list when no certificates")
    void getStudentCertificates_NoCertificates_ReturnsEmptyList() throws Exception {
        // Arrange
        when(studentService.getStudentCertificates(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/certificates")
                        .param("studentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /certificates - Fails when student not found")
    void getStudentCertificates_StudentNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(studentService.getStudentCertificates(999L))
                .thenThrow(new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/students/certificates")
                        .param("studentId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /register - Fails when required fields are missing")
    void registerStudent_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateStudentRequestDto invalidRequest = new CreateStudentRequestDto();
        // All fields are null

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Fails when password is too short")
    void registerStudent_ShortPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setPassword("123"); // Too short

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register - Fails when MSSV is invalid")
    void registerStudent_InvalidMssv_ReturnsBadRequest() throws Exception {
        // Arrange
        createRequest.setMssv("12345"); // Invalid format

        // Act & Assert
        mockMvc.perform(post("/api/v1/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }
}
