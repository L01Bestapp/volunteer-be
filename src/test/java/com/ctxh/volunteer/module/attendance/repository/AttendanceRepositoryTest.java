package com.ctxh.volunteer.module.attendance.repository;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.attendance.enums.AttendanceStatus;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AttendanceRepository Integration Tests")
class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

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

    private Student testStudent1;
    private Student testStudent2;
    private Activity testActivity;
    private Organization testOrganization;
    private Attendance attendance1;
    private Attendance attendance2;

    @BeforeEach
    void setUp() {
        // Create role
        Role studentRole = Role.builder()
                .roleName(RoleEnum.STUDENT.name())
                .build();
        studentRole = roleRepository.save(studentRole);

        // Create organization
        testOrganization = Organization.builder()
                .organizationName("Test Organization")
                .build();
        testOrganization = organizationRepository.save(testOrganization);

        // Create activity
        testActivity = Activity.builder()
                .title("Test Activity")
                .shortDescription("Description")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
        testActivity = activityRepository.save(testActivity);

        // Create first student
        User user1 = User.builder()
                .email("student1@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(studentRole))
                .build();

        testStudent1 = Student.builder()
                .user(user1)
                .fullName("Nguyen Van A")
                .mssv("2012345")
                .gender(Gender.MALE)
                .totalCtxhDays(0.0)
                .build();

        user1.setStudent(testStudent1);
        userRepository.save(user1);
        testStudent1 = studentRepository.save(testStudent1);

        // Create second student
        User user2 = User.builder()
                .email("student2@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(studentRole))
                .build();

        testStudent2 = Student.builder()
                .user(user2)
                .fullName("Tran Thi B")
                .mssv("2098765")
                .gender(Gender.FEMALE)
                .totalCtxhDays(0.0)
                .build();

        user2.setStudent(testStudent2);
        userRepository.save(user2);
        testStudent2 = studentRepository.save(testStudent2);

        // Create attendance records
        attendance1 = Attendance.builder()
                .student(testStudent1)
                .activity(testActivity)
                .attendanceDate(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .build();
        attendance1 = attendanceRepository.save(attendance1);

        attendance2 = Attendance.builder()
                .student(testStudent2)
                .activity(testActivity)
                .attendanceDate(LocalDateTime.now())
                .status(AttendanceStatus.ABSENT)
                .build();
        attendance2 = attendanceRepository.save(attendance2);
    }

    // ==================== FIND BY ACTIVITY ID TESTS ====================

    @Test
    @DisplayName("Find By Activity ID - Returns all attendance records")
    void findByActivityId_ReturnsAllAttendances() {
        // Act
        List<Attendance> result = attendanceRepository.findByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(a -> a.getStudent().getStudentId())
                .containsExactlyInAnyOrder(testStudent1.getStudentId(), testStudent2.getStudentId());
    }

    @Test
    @DisplayName("Find By Activity ID - Returns empty list when no attendances")
    void findByActivityId_ReturnsEmpty_WhenNoAttendances() {
        // Arrange
        Activity emptyActivity = Activity.builder()
                .title("Empty Activity")
                .shortDescription("No attendances")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
        emptyActivity = activityRepository.save(emptyActivity);

        // Act
        List<Attendance> result = attendanceRepository.findByActivityId(emptyActivity.getActivityId());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find By Activity ID - Orders by check-in time descending")
    void findByActivityId_OrdersByCheckInTimeDesc() {
        // Arrange
        Attendance oldAttendance = Attendance.builder()
                .student(testStudent1)
                .activity(testActivity)
                .attendanceDate(LocalDateTime.now().minusDays(2))
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now().minusDays(2))
                .build();
        attendanceRepository.save(oldAttendance);

        // Act
        List<Attendance> result = attendanceRepository.findByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(result).hasSize(3);
        // Most recent check-in should be first
        assertThat(result.get(0).getCheckInTime())
                .isAfter(result.get(result.size() - 1).getCheckInTime());
    }

    // ==================== FIND BY STUDENT AND ACTIVITY TESTS ====================

    @Test
    @DisplayName("Find By Student And Activity - Returns attendance")
    void findByStudentIdAndActivityId_ReturnsAttendance() {
        // Act
        Optional<Attendance> result = attendanceRepository.findByStudentIdAndActivityId(
                testStudent1.getStudentId(),
                testActivity.getActivityId()
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStudent().getStudentId()).isEqualTo(testStudent1.getStudentId());
        assertThat(result.get().getActivity().getActivityId()).isEqualTo(testActivity.getActivityId());
    }

    @Test
    @DisplayName("Find By Student And Activity - Returns empty when not found")
    void findByStudentIdAndActivityId_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Attendance> result = attendanceRepository.findByStudentIdAndActivityId(
                999999L,
                testActivity.getActivityId()
        );

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND BY STUDENT ID TESTS ====================

    @Test
    @DisplayName("Find By Student ID - Returns all student attendances")
    void findByStudentId_ReturnsAllStudentAttendances() {
        // Arrange
        Activity anotherActivity = Activity.builder()
                .title("Another Activity")
                .shortDescription("Description")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
        anotherActivity = activityRepository.save(anotherActivity);

        Attendance anotherAttendance = Attendance.builder()
                .student(testStudent1)
                .activity(anotherActivity)
                .attendanceDate(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(anotherAttendance);

        // Act
        List<Attendance> result = attendanceRepository.findByStudentId(testStudent1.getStudentId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
                .allMatch(a -> a.getStudent().getStudentId().equals(testStudent1.getStudentId()));
    }

    @Test
    @DisplayName("Find By Student ID - Orders by attendance date descending")
    void findByStudentId_OrdersByAttendanceDateDesc() {
        // Arrange
        Activity oldActivity = Activity.builder()
                .title("Old Activity")
                .shortDescription("Description")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now().minusDays(10))
                .endDateTime(LocalDateTime.now().minusDays(9))
                .build();
        oldActivity = activityRepository.save(oldActivity);

        Attendance oldAttendance = Attendance.builder()
                .student(testStudent1)
                .activity(oldActivity)
                .attendanceDate(LocalDateTime.now().minusDays(10))
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(oldAttendance);

        // Act
        List<Attendance> result = attendanceRepository.findByStudentId(testStudent1.getStudentId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAttendanceDate())
                .isAfter(result.get(1).getAttendanceDate());
    }

    // ==================== COUNT QUERIES TESTS ====================

    @Test
    @DisplayName("Count Present - Returns correct count")
    void countPresentByActivityId_ReturnsCorrectCount() {
        // Act
        Long count = attendanceRepository.countPresentByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(count).isEqualTo(1L); // Only attendance1 is PRESENT
    }

    @Test
    @DisplayName("Count Absent - Returns correct count")
    void countAbsentByActivityId_ReturnsCorrectCount() {
        // Act
        Long count = attendanceRepository.countAbsentByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(count).isEqualTo(1L); // Only attendance2 is ABSENT
    }

    @Test
    @DisplayName("Count Checked In - Returns correct count")
    void countCheckedInByActivityId_ReturnsCorrectCount() {
        // Act
        Long count = attendanceRepository.countCheckedInByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(count).isEqualTo(1L); // Only attendance1 has checkInTime
    }

    @Test
    @DisplayName("Count Checked Out - Returns correct count")
    void countCheckedOutByActivityId_ReturnsCorrectCount() {
        // Arrange
        attendance1.checkOut();
        attendanceRepository.save(attendance1);

        // Act
        Long count = attendanceRepository.countCheckedOutByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(count).isEqualTo(1L); // Only attendance1 has checkOutTime
    }

    @Test
    @DisplayName("Count Present - Returns zero for activity with no present students")
    void countPresentByActivityId_ReturnsZero_WhenNoPresent() {
        // Arrange
        Activity emptyActivity = Activity.builder()
                .title("Empty Activity")
                .shortDescription("No attendances")
                .organization(testOrganization)
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusDays(1))
                .build();
        emptyActivity = activityRepository.save(emptyActivity);

        // Act
        Long count = attendanceRepository.countPresentByActivityId(emptyActivity.getActivityId());

        // Assert
        assertThat(count).isZero();
    }

    // ==================== FIND BY DATE RANGE TESTS ====================

    @Test
    @DisplayName("Find By Date Range - Returns attendance within range")
    void findByStudentIdAndActivityIdAndDate_ReturnsAttendance() {
        // Arrange
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // Act
        Optional<Attendance> result = attendanceRepository.findByStudentIdAndActivityIdAndDate(
                testStudent1.getStudentId(),
                testActivity.getActivityId(),
                startOfDay,
                endOfDay
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStudent().getStudentId()).isEqualTo(testStudent1.getStudentId());
    }

    @Test
    @DisplayName("Find By Date Range - Returns empty when outside range")
    void findByStudentIdAndActivityIdAndDate_ReturnsEmpty_WhenOutsideRange() {
        // Arrange
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // Act
        Optional<Attendance> result = attendanceRepository.findByStudentIdAndActivityIdAndDate(
                testStudent1.getStudentId(),
                testActivity.getActivityId(),
                yesterday,
                yesterdayEnd
        );

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find By Date Range - Handles exact boundary")
    void findByStudentIdAndActivityIdAndDate_HandlesExactBoundary() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        Attendance boundaryAttendance = Attendance.builder()
                .student(testStudent2)
                .activity(testActivity)
                .attendanceDate(now)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(boundaryAttendance);

        // Act
        Optional<Attendance> result = attendanceRepository.findByStudentIdAndActivityIdAndDate(
                testStudent2.getStudentId(),
                testActivity.getActivityId(),
                now.minusMinutes(1),
                now.plusMinutes(1)
        );

        // Assert
        assertThat(result).isPresent();
    }

    // ==================== COMPLEX SCENARIOS TESTS ====================

    @Test
    @DisplayName("Multiple Students Same Activity - All counted correctly")
    void multipleStudentsSameActivity_AllCountedCorrectly() {
        // Arrange
        User user3 = User.builder()
                .email("student3@hcmut.edu.vn")
                .password("password")
                .avatarUrl("avatar.png")
                .build();

        Student student3 = Student.builder()
                .user(user3)
                .fullName("Le Van C")
                .mssv("2077777")
                .gender(Gender.MALE)
                .totalCtxhDays(0.0)
                .build();

        user3.setStudent(student3);
        userRepository.save(user3);
        student3 = studentRepository.save(student3);

        Attendance attendance3 = Attendance.builder()
                .student(student3)
                .activity(testActivity)
                .attendanceDate(LocalDateTime.now())
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalDateTime.now())
                .build();
        attendanceRepository.save(attendance3);

        // Act
        List<Attendance> allAttendances = attendanceRepository.findByActivityId(testActivity.getActivityId());
        Long presentCount = attendanceRepository.countPresentByActivityId(testActivity.getActivityId());
        Long checkedInCount = attendanceRepository.countCheckedInByActivityId(testActivity.getActivityId());

        // Assert
        assertThat(allAttendances).hasSize(3);
        assertThat(presentCount).isEqualTo(2L); // student1 and student3
        assertThat(checkedInCount).isEqualTo(2L); // student1 and student3
    }

    @Test
    @DisplayName("Check-in and Check-out Flow - Updates correctly")
    void checkInAndCheckOutFlow_UpdatesCorrectly() {
        // Initial state
        assertThat(attendance1.hasCheckedIn()).isTrue();
        assertThat(attendance1.hasCheckedOut()).isFalse();

        // Check out
        attendance1.checkOut();
        attendance1 = attendanceRepository.save(attendance1);

        // Verify
        Long checkedOutCount = attendanceRepository.countCheckedOutByActivityId(testActivity.getActivityId());
        assertThat(checkedOutCount).isEqualTo(1L);
        assertThat(attendance1.hasCheckedOut()).isTrue();
        assertThat(attendance1.isComplete()).isTrue();
    }
}
