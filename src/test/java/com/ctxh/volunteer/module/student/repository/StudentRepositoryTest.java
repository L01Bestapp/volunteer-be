package com.ctxh.volunteer.module.student.repository;

import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudentRepository Integration Tests")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role studentRole;
    private User testUser1;
    private User testUser2;
    private Student testStudent1;
    private Student testStudent2;

    @BeforeEach
    void setUp() {
        // Create role
        studentRole = Role.builder()
                .roleName(RoleEnum.STUDENT.name())
                .build();
        studentRole = roleRepository.save(studentRole);

        // Create first user and student
        testUser1 = User.builder()
                .email("student1@hcmut.edu.vn")
                .password("encoded_password")
                .avatarUrl("avatar1.png")
                .isVerified(true)
                .roles(List.of(studentRole))
                .build();

        testStudent1 = Student.builder()
                .user(testUser1)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .academicYear("2020")
                .faculty("Computer Science")
                .dateOfBirth(LocalDate.of(2002, 5, 15))
                .gender(Gender.MALE)
                .phoneNumber("0123456789")
                .totalCtxhDays(5.0)
                .build();

        testUser1.setStudent(testStudent1);
        userRepository.save(testUser1);
        testStudent1.generateQrCode();
        testStudent1 = studentRepository.save(testStudent1);

        // Create second user and student
        testUser2 = User.builder()
                .email("student2@hcmut.edu.vn")
                .password("encoded_password")
                .avatarUrl("avatar2.png")
                .isVerified(true)
                .roles(List.of(studentRole))
                .build();

        testStudent2 = Student.builder()
                .user(testUser2)
                .fullName("Tran Thi B")
                .mssv("2098765")
                .academicYear("2020")
                .faculty("Electrical Engineering")
                .dateOfBirth(LocalDate.of(2002, 8, 20))
                .gender(Gender.FEMALE)
                .phoneNumber("0987654321")
                .totalCtxhDays(3.0)
                .build();

        testUser2.setStudent(testStudent2);
        userRepository.save(testUser2);
        testStudent2.generateQrCode();
        testStudent2 = studentRepository.save(testStudent2);
    }

    // ==================== FIND BY MSSV TESTS ====================

    @Test
    @DisplayName("Find By MSSV - Success returns student")
    void findByMssv_Success_ReturnsStudent() {
        // Act
        Optional<Student> result = studentRepository.findByMssv("2012345");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getMssv()).isEqualTo("2012345");
        assertThat(result.get().getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("Find By MSSV - Returns empty when not found")
    void findByMssv_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Student> result = studentRepository.findByMssv("9999999");

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND BY USER ID TESTS ====================

    @Test
    @DisplayName("Find By User ID - Success returns student")
    void findByUserId_Success_ReturnsStudent() {
        // Act
        Optional<Student> result = studentRepository.findByUser_UserId(testUser1.getUserId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUserId()).isEqualTo(testUser1.getUserId());
        assertThat(result.get().getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("Find By User ID - Returns empty when not found")
    void findByUserId_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Student> result = studentRepository.findByUser_UserId(999999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND BY QR CODE TESTS ====================

    @Test
    @DisplayName("Find By QR Code - Success returns student")
    void findByQrCode_Success_ReturnsStudent() {
        // Arrange
        String qrCode = testStudent1.getQrCodeData();

        // Act
        Optional<Student> result = studentRepository.findByQrCodeData(qrCode);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getQrCodeData()).isEqualTo(qrCode);
        assertThat(result.get().getMssv()).isEqualTo("2012345");
    }

    @Test
    @DisplayName("Find By QR Code - Returns empty when not found")
    void findByQrCode_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Student> result = studentRepository.findByQrCodeData("INVALID-QR-CODE");

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== EXISTS BY MSSV TESTS ====================

    @Test
    @DisplayName("Exists By MSSV - Returns true when exists")
    void existsByMssv_ReturnsTrue_WhenExists() {
        // Act
        boolean exists = studentRepository.existsByMssv("2012345");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists By MSSV - Returns false when not exists")
    void existsByMssv_ReturnsFalse_WhenNotExists() {
        // Act
        boolean exists = studentRepository.existsByMssv("9999999");

        // Assert
        assertThat(exists).isFalse();
    }

    // ==================== EXISTS BY USER ID TESTS ====================

    @Test
    @DisplayName("Exists By User ID - Returns true when exists")
    void existsByUserId_ReturnsTrue_WhenExists() {
        // Act
        boolean exists = studentRepository.existsByUser_UserId(testUser1.getUserId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists By User ID - Returns false when not exists")
    void existsByUserId_ReturnsFalse_WhenNotExists() {
        // Act
        boolean exists = studentRepository.existsByUser_UserId(999999L);

        // Assert
        assertThat(exists).isFalse();
    }

    // ==================== FIND BY FACULTY TESTS ====================

    @Test
    @DisplayName("Find By Faculty - Success returns matching students")
    void findByFaculty_Success_ReturnsMatchingStudents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findByFacultyContainingIgnoreCase("computer", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFaculty()).containsIgnoringCase("Computer Science");
    }

    @Test
    @DisplayName("Find By Faculty - Case insensitive search works")
    void findByFaculty_CaseInsensitive_Works() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findByFacultyContainingIgnoreCase("ELECTRICAL", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFaculty()).isEqualTo("Electrical Engineering");
    }

    @Test
    @DisplayName("Find By Faculty - Returns empty when no match")
    void findByFaculty_ReturnsEmpty_WhenNoMatch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findByFacultyContainingIgnoreCase("Medicine", pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== FIND BY ACADEMIC YEAR TESTS ====================

    @Test
    @DisplayName("Find By Academic Year - Success returns students")
    void findByAcademicYear_Success_ReturnsStudents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findByAcademicYear("2020", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Student::getAcademicYear)
                .containsOnly("2020");
    }

    @Test
    @DisplayName("Find By Academic Year - Returns empty when no match")
    void findByAcademicYear_ReturnsEmpty_WhenNoMatch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findByAcademicYear("2025", pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== SEARCH STUDENTS TESTS ====================

    @Test
    @DisplayName("Search Students - By name returns matching students")
    void searchStudents_ByName_ReturnsMatching() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.searchStudents("nguyen", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).containsIgnoringCase("Nguyen");
    }

    @Test
    @DisplayName("Search Students - By MSSV returns matching students")
    void searchStudents_ByMssv_ReturnsMatching() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.searchStudents("2012345", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMssv()).isEqualTo("2012345");
    }

    @Test
    @DisplayName("Search Students - Partial MSSV works")
    void searchStudents_PartialMssv_Works() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.searchStudents("201", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMssv()).startsWith("201");
    }

    @Test
    @DisplayName("Search Students - Case insensitive")
    void searchStudents_CaseInsensitive_Works() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.searchStudents("TRAN", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).containsIgnoringCase("Tran");
    }

    // ==================== FIND WITH MIN CTXH DAYS TESTS ====================

    @Test
    @DisplayName("Find With Min CTXH Days - Returns students above threshold")
    void findWithMinCtxhDays_ReturnsStudentsAboveThreshold() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithMinCtxhDays(4.0, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTotalCtxhDays()).isGreaterThanOrEqualTo(4.0);
        assertThat(result.getContent().get(0).getMssv()).isEqualTo("2012345");
    }

    @Test
    @DisplayName("Find With Min CTXH Days - Returns all when threshold is zero")
    void findWithMinCtxhDays_ReturnsAll_WhenThresholdZero() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithMinCtxhDays(0.0, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Find With Min CTXH Days - Returns empty when threshold too high")
    void findWithMinCtxhDays_ReturnsEmpty_WhenThresholdTooHigh() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithMinCtxhDays(100.0, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== FIND INCOMPLETE PROFILE TESTS ====================

    @Test
    @DisplayName("Find Incomplete Profiles - Returns students with missing info")
    void findIncompleteProfiles_ReturnsStudentsWithMissingInfo() {
        // Arrange
        User incompleteUser = User.builder()
                .email("incomplete@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(studentRole))
                .build();

        Student incompleteStudent = Student.builder()
                .user(incompleteUser)
                .fullName("") // Empty name
                .mssv("2055555")
                .totalCtxhDays(0.0)
                .build();

        incompleteUser.setStudent(incompleteStudent);
        userRepository.save(incompleteUser);
        studentRepository.save(incompleteStudent);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithIncompleteProfile(pageable);

        // Assert
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
                .anyMatch(s -> s.getMssv().equals("2055555"));
    }

    @Test
    @DisplayName("Find Incomplete Profiles - Student with null gender is incomplete")
    void findIncompleteProfiles_NullGender_IsIncomplete() {
        // Arrange
        User userWithNullGender = User.builder()
                .email("nullgender@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(studentRole))
                .build();

        Student studentWithNullGender = Student.builder()
                .user(userWithNullGender)
                .fullName("Student With Null Gender")
                .mssv("2066666")
                .dateOfBirth(LocalDate.of(2002, 1, 1))
                .gender(null) // Null gender
                .totalCtxhDays(0.0)
                .build();

        userWithNullGender.setStudent(studentWithNullGender);
        userRepository.save(userWithNullGender);
        studentRepository.save(studentWithNullGender);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithIncompleteProfile(pageable);

        // Assert
        assertThat(result.getContent())
                .anyMatch(s -> s.getMssv().equals("2066666"));
    }

    @Test
    @DisplayName("Find Incomplete Profiles - Complete profiles are excluded")
    void findIncompleteProfiles_CompleteProfilesExcluded() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Student> result = studentRepository.findStudentsWithIncompleteProfile(pageable);

        // Assert
        // testStudent1 and testStudent2 have complete profiles
        assertThat(result.getContent())
                .noneMatch(s -> s.getMssv().equals("2012345") || s.getMssv().equals("2098765"));
    }

    // ==================== PAGINATION TESTS ====================

    @Test
    @DisplayName("Pagination - First page works correctly")
    void pagination_FirstPage_WorksCorrectly() {
        // Arrange
        Pageable firstPage = PageRequest.of(0, 1);

        // Act
        Page<Student> result = studentRepository.findAll(firstPage);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Pagination - Second page works correctly")
    void pagination_SecondPage_WorksCorrectly() {
        // Arrange
        Pageable secondPage = PageRequest.of(1, 1);

        // Act
        Page<Student> result = studentRepository.findAll(secondPage);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isFalse();
    }
}
