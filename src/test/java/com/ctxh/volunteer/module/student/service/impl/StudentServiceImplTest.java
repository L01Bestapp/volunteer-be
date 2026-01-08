package com.ctxh.volunteer.module.student.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.attendance.repository.AttendanceRepository;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.certificate.dto.CertificateResponseDto;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.certificate.repository.CertificateRepository;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.ParticipationHistoryDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student testStudent;
    private User testUser;
    private Role studentRole;
    private CreateStudentRequestDto createRequest;
    private UpdateStudentRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        // Create test role
        studentRole = Role.builder()
                .roleId(1L)
                .roleName(RoleEnum.STUDENT.name())
                .build();

        // Create test user
        testUser = User.builder()
                .userId(1L)
                .email("student@hcmut.edu.vn")
                .password("$2a$10$hashedPassword")
                .avatarUrl("default-avatar.png")
                .isVerified(true)
                .roles(List.of(studentRole))
                .build();

        // Create test student
        testStudent = Student.builder()
                .studentId(1L)
                .user(testUser)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .gender(Gender.MALE)
                .totalCtxhDays(0.0)
                .build();

        testUser.setStudent(testStudent);

        // Create request DTOs
        createRequest = new CreateStudentRequestDto();
        createRequest.setEmail("newstudent@hcmut.edu.vn");
        createRequest.setPassword("password123");
        createRequest.setFullName("New Student");
        createRequest.setMssv("2099999");
//        createRequest.setGender("MALE");

        updateRequest = new UpdateStudentRequestDto();
        updateRequest.setFullName("Updated Name");
        updateRequest.setPhoneNumber("0123456789");
    }

    // ==================== REGISTER STUDENT TESTS ====================

    @Test
    @DisplayName("Register Student - Success creates new student")
    void registerStudent_Success_CreatesNewStudent() {
        // Arrange
        when(studentRepository.existsByMssv(createRequest.getMssv())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.STUDENT.name())).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(createRequest.getPassword())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentResponseDto response = studentService.registerStudent(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());

        verify(studentRepository).existsByMssv(createRequest.getMssv());
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(roleRepository).findByRoleName(RoleEnum.STUDENT.name());
        verify(passwordEncoder).encode(createRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Register Student - Fails when MSSV already exists")
    void registerStudent_ThrowsException_WhenMssvExists() {
        // Arrange
        when(studentRepository.existsByMssv(createRequest.getMssv())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> studentService.registerStudent(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MSSV_ALREADY_EXISTS);

        verify(studentRepository).existsByMssv(createRequest.getMssv());
        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register Student - Fails when email already registered")
    void registerStudent_ThrowsException_WhenEmailExists() {
        // Arrange
        when(studentRepository.existsByMssv(createRequest.getMssv())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> studentService.registerStudent(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_REGISTERED);

        verify(studentRepository).existsByMssv(createRequest.getMssv());
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register Student - Fails when student role not found")
    void registerStudent_ThrowsException_WhenRoleNotFound() {
        // Arrange
        when(studentRepository.existsByMssv(createRequest.getMssv())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.STUDENT.name())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.registerStudent(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_NOT_FOUND);

        verify(roleRepository).findByRoleName(RoleEnum.STUDENT.name());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register Student - Calls save on student repository")
    void registerStudent_CallsSaveOnStudentRepository() {
        // Arrange
        when(studentRepository.existsByMssv(createRequest.getMssv())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.STUDENT.name())).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.registerStudent(createRequest);

        // Assert
        // Verify student repository save was called (QR code is generated before save)
        verify(studentRepository).save(any(Student.class));
        verify(userRepository).save(any(User.class));
    }

    // ==================== UPDATE STUDENT TESTS ====================

    @Test
    @DisplayName("Update Student - Success updates student info")
    void updateStudent_Success_UpdatesStudentInfo() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        StudentResponseDto response = studentService.updateStudent(1L, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("Update Student - Fails when student not found")
    void updateStudent_ThrowsException_WhenStudentNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.updateStudent(999L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(userRepository).findById(999L);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Student - Updates only provided fields")
    void updateStudent_UpdatesOnlyProvidedFields() {
        // Arrange
        UpdateStudentRequestDto partialUpdate = new UpdateStudentRequestDto();
        partialUpdate.setPhoneNumber("0987654321");
        // Other fields are null

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.updateStudent(1L, partialUpdate);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("Update Student - Updates gender correctly")
    void updateStudent_UpdatesGenderCorrectly() {
        // Arrange
        updateRequest.setGender("FEMALE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        studentService.updateStudent(1L, updateRequest);

        // Assert
        assertThat(testStudent.getGender()).isEqualTo(Gender.FEMALE);
        verify(studentRepository).save(testStudent);
    }

    // ==================== GET STUDENT TESTS ====================

    @Test
    @DisplayName("Get Student By ID - Success returns student")
    void getStudentById_Success_ReturnsStudent() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // Act
        StudentResponseDto response = studentService.getStudentById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(response.getMssv()).isEqualTo(testStudent.getMssv());

        verify(studentRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Student By ID - Fails when student not found")
    void getStudentById_ThrowsException_WhenStudentNotFound() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).findById(999L);
    }

    @Test
    @DisplayName("Get Student By MSSV - Success returns student")
    void getStudentByMssv_Success_ReturnsStudent() {
        // Arrange
        when(studentRepository.findByMssv("2012345")).thenReturn(Optional.of(testStudent));

        // Act
        StudentResponseDto response = studentService.getStudentByMssv("2012345");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
        assertThat(response.getMssv()).isEqualTo("2012345");

        verify(studentRepository).findByMssv("2012345");
    }

    @Test
    @DisplayName("Get Student By MSSV - Fails when student not found")
    void getStudentByMssv_ThrowsException_WhenStudentNotFound() {
        // Arrange
        when(studentRepository.findByMssv("9999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentByMssv("9999999"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).findByMssv("9999999");
    }

    // ==================== PARTICIPATION HISTORY TESTS ====================

    @Test
    @DisplayName("Get Participation History - Success returns history")
    void getParticipationHistory_Success_ReturnsHistory() {
        // Arrange
        Organization org = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Org")
                .build();

        Activity activity = Activity.builder()
                .activityId(1L)
                .title("Test Activity")
                .organization(org)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(activity)
                .status(EnrollmentStatus.APPROVED)
                .appliedAt(LocalDateTime.now())
                .build();

        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findByStudentIdAndActivityId(1L, 1L)).thenReturn(Optional.empty());
        when(certificateRepository.findByEnrollment_EnrollmentId(1L)).thenReturn(Optional.empty());

        // Act
        List<ParticipationHistoryDto> result = studentService.getParticipationHistory(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnrollmentId()).isEqualTo(enrollment.getEnrollmentId());
        assertThat(result.get(0).getActivityTitle()).isEqualTo(activity.getTitle());

        verify(studentRepository).existsById(1L);
        verify(enrollmentRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Get Participation History - Fails when student not found")
    void getParticipationHistory_ThrowsException_WhenStudentNotFound() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> studentService.getParticipationHistory(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).existsById(999L);
        verify(enrollmentRepository, never()).findByStudentId(anyLong());
    }

    @Test
    @DisplayName("Get Participation History - Includes attendance info when present")
    void getParticipationHistory_IncludesAttendanceInfo() {
        // Arrange
        Organization org = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Org")
                .build();

        Activity activity = Activity.builder()
                .activityId(1L)
                .title("Test Activity")
                .organization(org)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(activity)
                .status(EnrollmentStatus.APPROVED)
                .appliedAt(LocalDateTime.now())
                .build();

        Attendance attendance = Attendance.builder()
                .attendanceId(1L)
                .student(testStudent)
                .activity(activity)
                .checkInTime(LocalDateTime.now())
                .checkOutTime(LocalDateTime.now().plusHours(2))
                .build();

        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findByStudentIdAndActivityId(1L, 1L)).thenReturn(Optional.of(attendance));
        when(certificateRepository.findByEnrollment_EnrollmentId(1L)).thenReturn(Optional.empty());

        // Act
        List<ParticipationHistoryDto> result = studentService.getParticipationHistory(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHasAttendance()).isTrue();
        assertThat(result.get(0).getAttendanceDuration()).isNotNull();
    }

    @Test
    @DisplayName("Get Participation History - Includes certificate info when present")
    void getParticipationHistory_IncludesCertificateInfo() {
        // Arrange
        Organization org = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Org")
                .build();

        Activity activity = Activity.builder()
                .activityId(1L)
                .title("Test Activity")
                .organization(org)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();

        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(activity)
                .status(EnrollmentStatus.APPROVED)
                .appliedAt(LocalDateTime.now())
                .build();

        Certificate certificate = Certificate.builder()
                .certificateId(1L)
                .certificateCode("CERT-001")
                .enrollment(enrollment)
                .build();

        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findByStudentIdAndActivityId(1L, 1L)).thenReturn(Optional.empty());
        when(certificateRepository.findByEnrollment_EnrollmentId(1L)).thenReturn(Optional.of(certificate));

        // Act
        List<ParticipationHistoryDto> result = studentService.getParticipationHistory(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHasCertificate()).isTrue();
        assertThat(result.get(0).getCertificateCode()).isEqualTo("CERT-001");
    }

    @Test
    @DisplayName("Get Participation History - Returns empty list when no enrollments")
    void getParticipationHistory_ReturnsEmpty_WhenNoEnrollments() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());

        // Act
        List<ParticipationHistoryDto> result = studentService.getParticipationHistory(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(studentRepository).existsById(1L);
        verify(enrollmentRepository).findByStudentId(1L);
    }

    // ==================== GET STUDENT CERTIFICATES TESTS ====================

    @Test
    @DisplayName("Get Student Certificates - Success returns certificates")
    void getStudentCertificates_Success_ReturnsCertificates() {
        // Arrange
        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .completedAt(LocalDateTime.now())
                .build();

        Certificate certificate = Certificate.builder()
                .certificateId(1L)
                .certificateCode("CERT-001")
                .enrollment(enrollment)
                .studentId(1L)
                .studentName("Nguyen Van A")
                .studentMssv("2012345")
                .activityId(1L)
                .activityTitle("Test Activity")
                .activityStartDate(LocalDateTime.now())
                .activityEndDate(LocalDateTime.now().plusDays(1))
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .issuedDate(LocalDateTime.now())
                .isRevoked(false)
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(certificateRepository.findValidCertificatesByStudentId(1L)).thenReturn(List.of(certificate));

        // Act
        List<CertificateResponseDto> result = studentService.getStudentCertificates(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCertificateCode()).isEqualTo("CERT-001");

        verify(studentRepository).findById(1L);
        verify(certificateRepository).findValidCertificatesByStudentId(1L);
    }

    @Test
    @DisplayName("Get Student Certificates - Fails when student not found")
    void getStudentCertificates_ThrowsException_WhenStudentNotFound() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentCertificates(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(studentRepository).findById(999L);
        verify(certificateRepository, never()).findValidCertificatesByStudentId(anyLong());
    }

    @Test
    @DisplayName("Get Student Certificates - Returns empty list when no certificates")
    void getStudentCertificates_ReturnsEmpty_WhenNoCertificates() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(certificateRepository.findValidCertificatesByStudentId(1L)).thenReturn(List.of());

        // Act
        List<CertificateResponseDto> result = studentService.getStudentCertificates(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(studentRepository).findById(1L);
        verify(certificateRepository).findValidCertificatesByStudentId(1L);
    }
}
