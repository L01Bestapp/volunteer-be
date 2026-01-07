package com.ctxh.volunteer.module.certificate.repository;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.repository.OrganizationRepository;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CertificateRepository Integration Tests")
class CertificateRepositoryTest {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Student testStudent;
    private Activity testActivity;
    private Enrollment testEnrollment;
    private Certificate testCertificate;

    @BeforeEach
    void setUp() {
        // Create role
        Role studentRole = Role.builder()
                .roleName(RoleEnum.STUDENT.name())
                .build();
        studentRole = roleRepository.save(studentRole);

        // Create organization
        Organization organization = Organization.builder()
                .organizationName("Test Organization")
                .representativeName("John Doe")
                .representativeEmail("john@test.com")
                .build();
        organization = organizationRepository.save(organization);

        // Create activity
        testActivity = Activity.builder()
                .title("Test Activity")
                .shortDescription("Description")
                .organization(organization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .theNumberOfCtxhDay(1.0)
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create user and student
        User user = User.builder()
                .email("student@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(studentRole))
                .build();

        testStudent = Student.builder()
                .user(user)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .gender(Gender.MALE)
                .faculty("Computer Science")
                .academicYear("2020")
                .totalCtxhDays(0.0)
                .build();

        user.setStudent(testStudent);
        userRepository.save(user);
        testStudent = studentRepository.save(testStudent);

        // Create enrollment
        testEnrollment = Enrollment.builder()
                .student(testStudent)
                .activity(testActivity)
                .isCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
        testEnrollment = enrollmentRepository.save(testEnrollment);

        // Create certificate
        testCertificate = Certificate.builder()
                .enrollment(testEnrollment)
                .studentId(testStudent.getStudentId())
                .activityId(testActivity.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .studentFaculty(testStudent.getFaculty())
                .studentAcademicYear(testStudent.getAcademicYear())
                .activityTitle(testActivity.getTitle())
                .activityStartDate(testActivity.getStartDateTime())
                .activityEndDate(testActivity.getEndDateTime())
                .ctxhHours(testActivity.getTheNumberOfCtxhDay())
                .organizationName(organization.getOrganizationName())
                .representativeName(organization.getRepresentativeName())
                .representativeEmail(organization.getRepresentativeEmail())
                .isRevoked(false)
                .build();

        testCertificate.issue();
        testCertificate = certificateRepository.save(testCertificate);
    }

    // ==================== FIND BY CERTIFICATE CODE TESTS ====================

    @Test
    @DisplayName("Find By Certificate Code - Returns certificate")
    void findByCertificateCode_ReturnsCertificate() {
        // Act
        Optional<Certificate> result = certificateRepository.findByCertificateCode(
                testCertificate.getCertificateCode()
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCertificateCode()).isEqualTo(testCertificate.getCertificateCode());
        assertThat(result.get().getStudentName()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("Find By Certificate Code - Returns empty when not found")
    void findByCertificateCode_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Certificate> result = certificateRepository.findByCertificateCode("CERT-INVALID");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find By Certificate Code - Is case sensitive")
    void findByCertificateCode_IsCaseSensitive() {
        // Act
        String lowerCaseCode = testCertificate.getCertificateCode().toLowerCase();
        Optional<Certificate> result = certificateRepository.findByCertificateCode(lowerCaseCode);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND BY ENROLLMENT ID TESTS ====================

    @Test
    @DisplayName("Find By Enrollment ID - Returns certificate")
    void findByEnrollmentId_ReturnsCertificate() {
        // Act
        Optional<Certificate> result = certificateRepository.findByEnrollment_EnrollmentId(
                testEnrollment.getEnrollmentId()
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEnrollment().getEnrollmentId())
                .isEqualTo(testEnrollment.getEnrollmentId());
    }

    @Test
    @DisplayName("Find By Enrollment ID - Returns empty when not found")
    void findByEnrollmentId_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Certificate> result = certificateRepository.findByEnrollment_EnrollmentId(999999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND BY STUDENT ID TESTS ====================

    @Test
    @DisplayName("Find By Student ID - Returns all certificates")
    void findByStudentId_ReturnsAllCertificates() {
        // Arrange
        Activity activity2 = Activity.builder()
                .title("Second Activity")
                .shortDescription("Description")
                .organization(testActivity.getOrganization())
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .theNumberOfCtxhDay(1.0)
                .build();
        activity2 = activityRepository.save(activity2);

        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent)
                .activity(activity2)
                .isCompleted(true)
                .build();
        enrollment2 = enrollmentRepository.save(enrollment2);

        Certificate certificate2 = Certificate.builder()
                .enrollment(enrollment2)
                .studentId(testStudent.getStudentId())
                .activityId(activity2.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .activityTitle(activity2.getTitle())
                .activityStartDate(activity2.getStartDateTime())
                .activityEndDate(activity2.getEndDateTime())
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .isRevoked(false)
                .build();
        certificate2.issue();
        certificateRepository.save(certificate2);

        // Act
        List<Certificate> result = certificateRepository.findByStudentId(testStudent.getStudentId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getStudentId().equals(testStudent.getStudentId()));
    }

    @Test
    @DisplayName("Find By Student ID - Orders by issued date descending")
    void findByStudentId_OrdersByIssuedDateDesc() {
        // Arrange - Create older certificate
        Activity oldActivity = Activity.builder()
                .title("Old Activity")
                .shortDescription("Description")
                .organization(testActivity.getOrganization())
                .startDateTime(LocalDateTime.now().minusDays(10))
                .endDateTime(LocalDateTime.now().minusDays(9))
                .theNumberOfCtxhDay(1.0)
                .build();
        oldActivity = activityRepository.save(oldActivity);

        Enrollment oldEnrollment = Enrollment.builder()
                .student(testStudent)
                .activity(oldActivity)
                .isCompleted(true)
                .build();
        oldEnrollment = enrollmentRepository.save(oldEnrollment);

        Certificate oldCertificate = Certificate.builder()
                .enrollment(oldEnrollment)
                .studentId(testStudent.getStudentId())
                .activityId(oldActivity.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .activityTitle(oldActivity.getTitle())
                .activityStartDate(oldActivity.getStartDateTime())
                .activityEndDate(oldActivity.getEndDateTime())
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .issuedDate(LocalDateTime.now().minusDays(5))
                .certificateCode("CERT-OLD12345")
                .isRevoked(false)
                .build();
        certificateRepository.save(oldCertificate);

        // Act
        List<Certificate> result = certificateRepository.findByStudentId(testStudent.getStudentId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIssuedDate())
                .isAfter(result.get(1).getIssuedDate());
    }

    @Test
    @DisplayName("Find By Student ID - Returns empty when no certificates")
    void findByStudentId_ReturnsEmpty_WhenNoCertificates() {
        // Act
        List<Certificate> result = certificateRepository.findByStudentId(999999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND VALID CERTIFICATES BY STUDENT ID TESTS ====================

    @Test
    @DisplayName("Find Valid Certificates - Returns only non-revoked certificates")
    void findValidCertificates_ReturnsOnlyNonRevoked() {
        // Arrange - Create revoked certificate
        Activity activity2 = Activity.builder()
                .title("Second Activity")
                .shortDescription("Description")
                .organization(testActivity.getOrganization())
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .theNumberOfCtxhDay(1.0)
                .build();
        activity2 = activityRepository.save(activity2);

        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent)
                .activity(activity2)
                .isCompleted(true)
                .build();
        enrollment2 = enrollmentRepository.save(enrollment2);

        Certificate revokedCertificate = Certificate.builder()
                .enrollment(enrollment2)
                .studentId(testStudent.getStudentId())
                .activityId(activity2.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .activityTitle(activity2.getTitle())
                .activityStartDate(activity2.getStartDateTime())
                .activityEndDate(activity2.getEndDateTime())
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .isRevoked(true)
                .revokedAt(LocalDateTime.now())
                .revokeReason("Test revocation")
                .build();
        revokedCertificate.issue();
        certificateRepository.save(revokedCertificate);

        // Act
        List<Certificate> result = certificateRepository.findValidCertificatesByStudentId(
                testStudent.getStudentId()
        );

        // Assert
        assertThat(result).hasSize(1); // Only testCertificate, not revokedCertificate
        assertThat(result.get(0).getIsRevoked()).isFalse();
        assertThat(result.get(0).getCertificateId()).isEqualTo(testCertificate.getCertificateId());
    }

    @Test
    @DisplayName("Find Valid Certificates - Returns empty when all are revoked")
    void findValidCertificates_ReturnsEmpty_WhenAllRevoked() {
        // Arrange - Revoke the test certificate
        testCertificate.revoke("Test reason");
        certificateRepository.save(testCertificate);

        // Act
        List<Certificate> result = certificateRepository.findValidCertificatesByStudentId(
                testStudent.getStudentId()
        );

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find Valid Certificates - Orders by issued date descending")
    void findValidCertificates_OrdersByIssuedDateDesc() {
        // Arrange - Create multiple valid certificates
        Activity activity2 = Activity.builder()
                .title("Second Activity")
                .shortDescription("Description")
                .organization(testActivity.getOrganization())
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .theNumberOfCtxhDay(1.0)
                .build();
        activity2 = activityRepository.save(activity2);

        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent)
                .activity(activity2)
                .isCompleted(true)
                .build();
        enrollment2 = enrollmentRepository.save(enrollment2);

        Certificate newCertificate = Certificate.builder()
                .enrollment(enrollment2)
                .studentId(testStudent.getStudentId())
                .activityId(activity2.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .activityTitle(activity2.getTitle())
                .activityStartDate(activity2.getStartDateTime())
                .activityEndDate(activity2.getEndDateTime())
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .isRevoked(false)
                .build();
        newCertificate.issue();
        // Make the issued date slightly later
        newCertificate.setIssuedDate(LocalDateTime.now().plusMinutes(1));
        certificateRepository.save(newCertificate);

        // Act
        List<Certificate> result = certificateRepository.findValidCertificatesByStudentId(
                testStudent.getStudentId()
        );

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIssuedDate())
                .isAfter(result.get(1).getIssuedDate());
    }

    // ==================== EXISTS BY ENROLLMENT ID TESTS ====================

    @Test
    @DisplayName("Exists By Enrollment ID - Returns true when exists")
    void existsByEnrollmentId_ReturnsTrue_WhenExists() {
        // Act
        boolean exists = certificateRepository.existsByEnrollment_EnrollmentId(
                testEnrollment.getEnrollmentId()
        );

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists By Enrollment ID - Returns false when not exists")
    void existsByEnrollmentId_ReturnsFalse_WhenNotExists() {
        // Act
        boolean exists = certificateRepository.existsByEnrollment_EnrollmentId(999999L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Exists By Enrollment ID - Returns true even if revoked")
    void existsByEnrollmentId_ReturnsTrue_EvenIfRevoked() {
        // Arrange
        testCertificate.revoke("Test reason");
        certificateRepository.save(testCertificate);

        // Act
        boolean exists = certificateRepository.existsByEnrollment_EnrollmentId(
                testEnrollment.getEnrollmentId()
        );

        // Assert
        assertThat(exists).isTrue();
    }

    // ==================== CERTIFICATE CODE UNIQUENESS TESTS ====================

    @Test
    @DisplayName("Certificate Code - Is unique constraint")
    void certificateCode_IsUnique() {
        // Arrange
        Enrollment newEnrollment = Enrollment.builder()
                .student(testStudent)
                .activity(testActivity)
                .isCompleted(true)
                .build();
        newEnrollment = enrollmentRepository.save(newEnrollment);

        Certificate duplicateCert = Certificate.builder()
                .enrollment(newEnrollment)
                .studentId(testStudent.getStudentId())
                .activityId(testActivity.getActivityId())
                .studentName(testStudent.getFullName())
                .studentMssv(testStudent.getMssv())
                .activityTitle(testActivity.getTitle())
                .activityStartDate(testActivity.getStartDateTime())
                .activityEndDate(testActivity.getEndDateTime())
                .ctxhHours(1.0)
                .organizationName("Test Org")
                .certificateCode(testCertificate.getCertificateCode()) // Same code!
                .issuedDate(LocalDateTime.now())
                .isRevoked(false)
                .build();

        // Act & Assert
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> certificateRepository.saveAndFlush(duplicateCert)
        )).isNotNull();
    }

    // ==================== DATA CACHING TESTS ====================

    @Test
    @DisplayName("Certificate - Caches all required data")
    void certificate_CachesAllRequiredData() {
        // Assert
        assertThat(testCertificate.getStudentId()).isNotNull();
        assertThat(testCertificate.getStudentName()).isEqualTo(testStudent.getFullName());
        assertThat(testCertificate.getStudentMssv()).isEqualTo(testStudent.getMssv());
        assertThat(testCertificate.getActivityId()).isNotNull();
        assertThat(testCertificate.getActivityTitle()).isEqualTo(testActivity.getTitle());
        assertThat(testCertificate.getOrganizationName()).isNotNull();
    }
}
