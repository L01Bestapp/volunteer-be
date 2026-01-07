package com.ctxh.volunteer.module.certificate.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.certificate.repository.CertificateRepository;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateService Unit Tests")
class CertificateServiceImplTest {

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private Student testStudent;
    private Activity testActivity;
    private Organization testOrganization;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        // Create test student
        testStudent = Student.builder()
                .studentId(1L)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .faculty("Computer Science")
                .academicYear("2020")
                .gender(Gender.MALE)
                .build();

        // Create test organization
        testOrganization = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .representativeName("John Doe")
                .representativeEmail("john@test.com")
                .build();

        // Create test activity
        testActivity = Activity.builder()
                .activityId(1L)
                .title("Volunteer Activity")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .theNumberOfCtxhDay(1.0)
                .build();

        // Create test enrollment
        testEnrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(testActivity)
                .isCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
    }

    // ==================== GENERATE CERTIFICATE TESTS ====================

    @Test
    @DisplayName("Generate Certificate - Success creates certificate")
    void generateCertificate_Success_CreatesCertificate() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> {
                    Certificate cert = invocation.getArgument(0);
                    cert.setCertificateId(1L);
                    return cert;
                });

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCertificateId()).isEqualTo(1L);
        assertThat(result.getCertificateCode()).isNotNull();
        assertThat(result.getIssuedDate()).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(1L);
        assertThat(result.getStudentName()).isEqualTo("Nguyen Van A");
        assertThat(result.getStudentMssv()).isEqualTo("2012345");
        assertThat(result.getStudentFaculty()).isEqualTo("Computer Science");
        assertThat(result.getActivityId()).isEqualTo(1L);
        assertThat(result.getActivityTitle()).isEqualTo("Volunteer Activity");
        assertThat(result.getOrganizationName()).isEqualTo("Test Organization");
        assertThat(result.getRepresentativeName()).isEqualTo("John Doe");
        assertThat(result.getCtxhHours()).isEqualTo(1.0);

        verify(certificateRepository).existsByEnrollment_EnrollmentId(1L);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Generate Certificate - Caches all student info")
    void generateCertificate_CachesStudentInfo() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getStudentName()).isEqualTo(testStudent.getFullName());
        assertThat(result.getStudentMssv()).isEqualTo(testStudent.getMssv());
        assertThat(result.getStudentFaculty()).isEqualTo(testStudent.getFaculty());
        assertThat(result.getStudentAcademicYear()).isEqualTo(testStudent.getAcademicYear());
    }

    @Test
    @DisplayName("Generate Certificate - Caches all activity info")
    void generateCertificate_CachesActivityInfo() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getActivityTitle()).isEqualTo(testActivity.getTitle());
        assertThat(result.getActivityStartDate()).isEqualTo(testActivity.getStartDateTime());
        assertThat(result.getActivityEndDate()).isEqualTo(testActivity.getEndDateTime());
        assertThat(result.getCtxhHours()).isEqualTo(testActivity.getTheNumberOfCtxhDay());
    }

    @Test
    @DisplayName("Generate Certificate - Caches all organization info")
    void generateCertificate_CachesOrganizationInfo() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getOrganizationName()).isEqualTo(testOrganization.getOrganizationName());
        assertThat(result.getRepresentativeName()).isEqualTo(testOrganization.getRepresentativeName());
        assertThat(result.getRepresentativeEmail()).isEqualTo(testOrganization.getRepresentativeEmail());
    }

    @Test
    @DisplayName("Generate Certificate - Calls issue() to set issued date and code")
    void generateCertificate_CallsIssue() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getIssuedDate()).isNotNull();
        assertThat(result.getCertificateCode()).isNotNull();
        assertThat(result.getCertificateCode()).startsWith("CERT-");
    }

    @Test
    @DisplayName("Generate Certificate - Fails when certificate already exists")
    void generateCertificate_ThrowsException_WhenCertificateExists() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> certificateService.generateCertificate(testEnrollment))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CERTIFICATE_ALREADY_ISSUED);

        verify(certificateRepository).existsByEnrollment_EnrollmentId(1L);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generate Certificate - Fails when enrollment not completed")
    void generateCertificate_ThrowsException_WhenEnrollmentNotCompleted() {
        // Arrange
        testEnrollment.setIsCompleted(false);

        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> certificateService.generateCertificate(testEnrollment))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_APPROVED);

        verify(certificateRepository).existsByEnrollment_EnrollmentId(1L);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generate Certificate - Allows null completion (treated as incomplete)")
    void generateCertificate_AllowsNullCompletion() {
        // Arrange
        testEnrollment.setIsCompleted(null);

        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> {
                    Certificate cert = invocation.getArgument(0);
                    cert.setCertificateId(1L);
                    return cert;
                });

        // Act - Should not throw exception when isCompleted is null
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result).isNotNull();
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Generate Certificate - Sets enrollment relationship")
    void generateCertificate_SetsEnrollmentRelationship() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getEnrollment()).isEqualTo(testEnrollment);
        assertThat(result.getEnrollment().getEnrollmentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Generate Certificate - Certificate is valid by default")
    void generateCertificate_CertificateIsValidByDefault() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Certificate result = certificateService.generateCertificate(testEnrollment);

        // Assert
        assertThat(result.getIsRevoked()).isFalse();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getRevokedAt()).isNull();
        assertThat(result.getRevokeReason()).isNull();
    }

    @Test
    @DisplayName("Generate Certificate - Generates unique certificate codes")
    void generateCertificate_GeneratesUniqueCodes() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(anyLong()))
                .thenReturn(false);
        when(certificateRepository.save(any(Certificate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Create multiple enrollments
        Enrollment enrollment2 = Enrollment.builder()
                .enrollmentId(2L)
                .student(testStudent)
                .activity(testActivity)
                .isCompleted(true)
                .build();

        // Act
        Certificate cert1 = certificateService.generateCertificate(testEnrollment);
        Certificate cert2 = certificateService.generateCertificate(enrollment2);

        // Assert
        assertThat(cert1.getCertificateCode()).isNotEqualTo(cert2.getCertificateCode());
        assertThat(cert1.getCertificateCode()).startsWith("CERT-");
        assertThat(cert2.getCertificateCode()).startsWith("CERT-");
    }

    // ==================== CERTIFICATE EXISTS TESTS ====================

    @Test
    @DisplayName("Certificate Exists - Returns true when exists")
    void certificateExists_ReturnsTrue_WhenExists() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(true);

        // Act
        boolean result = certificateService.certificateExists(1L);

        // Assert
        assertThat(result).isTrue();
        verify(certificateRepository).existsByEnrollment_EnrollmentId(1L);
    }

    @Test
    @DisplayName("Certificate Exists - Returns false when not exists")
    void certificateExists_ReturnsFalse_WhenNotExists() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(999L))
                .thenReturn(false);

        // Act
        boolean result = certificateService.certificateExists(999L);

        // Assert
        assertThat(result).isFalse();
        verify(certificateRepository).existsByEnrollment_EnrollmentId(999L);
    }

    @Test
    @DisplayName("Certificate Exists - Is read-only operation")
    void certificateExists_IsReadOnly() {
        // Arrange
        when(certificateRepository.existsByEnrollment_EnrollmentId(1L))
                .thenReturn(true);

        // Act
        certificateService.certificateExists(1L);

        // Assert
        verify(certificateRepository).existsByEnrollment_EnrollmentId(1L);
        verify(certificateRepository, never()).save(any());
        verify(certificateRepository, never()).delete(any());
    }

    // ==================== CERTIFICATE CODE GENERATION TESTS ====================

    @Test
    @DisplayName("Certificate Code - Has correct format")
    void certificateCode_HasCorrectFormat() {
        // Act
        String code = Certificate.generateCertificateCode();

        // Assert
        assertThat(code).startsWith("CERT-");
        assertThat(code).hasSize(13); // "CERT-" (5) + 8 chars
    }

    @Test
    @DisplayName("Certificate Code - Is uppercase")
    void certificateCode_IsUppercase() {
        // Act
        String code = Certificate.generateCertificateCode();

        // Assert
        String afterPrefix = code.substring(5);
        assertThat(afterPrefix).isEqualTo(afterPrefix.toUpperCase());
    }

    @Test
    @DisplayName("Certificate Code - Generates unique codes")
    void certificateCode_GeneratesUniqueCodes() {
        // Act
        String code1 = Certificate.generateCertificateCode();
        String code2 = Certificate.generateCertificateCode();
        String code3 = Certificate.generateCertificateCode();

        // Assert
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code1).isNotEqualTo(code3);
        assertThat(code2).isNotEqualTo(code3);
    }

    // ==================== INTEGRATION WITH HELPER METHODS TESTS ====================

    @Test
    @DisplayName("Certificate - Revoke works correctly")
    void certificate_RevokeWorks() {
        // Arrange
        Certificate certificate = Certificate.builder()
                .certificateId(1L)
                .certificateCode("CERT-TEST123")
                .isRevoked(false)
                .build();

        // Act
        certificate.revoke("Duplicate submission");

        // Assert
        assertThat(certificate.getIsRevoked()).isTrue();
        assertThat(certificate.isValid()).isFalse();
        assertThat(certificate.getRevokedAt()).isNotNull();
        assertThat(certificate.getRevokeReason()).isEqualTo("Duplicate submission");
    }

    @Test
    @DisplayName("Certificate - Issue sets date and code")
    void certificate_IssueWorks() {
        // Arrange
        Certificate certificate = Certificate.builder()
                .certificateId(1L)
                .build();

        // Act
        certificate.issue();

        // Assert
        assertThat(certificate.getIssuedDate()).isNotNull();
        assertThat(certificate.getCertificateCode()).isNotNull();
        assertThat(certificate.getCertificateCode()).startsWith("CERT-");
    }

    @Test
    @DisplayName("Certificate - Issue preserves existing code")
    void certificate_IssuePreservesExistingCode() {
        // Arrange
        String existingCode = "CERT-CUSTOM01";
        Certificate certificate = Certificate.builder()
                .certificateId(1L)
                .certificateCode(existingCode)
                .build();

        // Act
        certificate.issue();

        // Assert
        assertThat(certificate.getCertificateCode()).isEqualTo(existingCode);
    }
}
