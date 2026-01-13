package com.ctxh.volunteer.module.enrollment.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentRequestDto;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.ctxh.volunteer.module.enrollment.dto.MyActivityResponseDto;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.notification.event.EnrollmentCreatedEvent;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Student testStudent;
    private Activity testActivity;
    private Organization testOrganization;
    private User testUser;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .userId(1L)
                .email("student@hcmut.edu.vn")
                .isVerified(true)
                .build();

        // Create test organization
        testOrganization = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .build();

        // Create test student with all required fields
        testStudent = Student.builder()
                .studentId(1L)
                .user(testUser)
                .fullName("Test Student")
                .mssv("2012345")
                .phoneNumber("0901234567")
                .academicYear("2020")
                .faculty("Computer Science")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2002, 1, 1))
                .totalCtxhDays(0.0)
                .enrollments(new ArrayList<>())
                .build();

        // Create test activity with valid configuration
        testActivity = Activity.builder()
                .activityId(1L)
                .title("Test Activity")
                .shortDescription("Test Description")
                .description("Full Description")
                .organization(testOrganization)
                .category(ActivityCategory.COMMUNITY_SERVICE)
                .startDateTime(LocalDateTime.now().plusDays(7))
                .endDateTime(LocalDateTime.now().plusDays(8))
                .registrationOpensAt(LocalDateTime.now().minusDays(1))
                .registrationDeadline(LocalDateTime.now().plusDays(6))
                .registrationState(RegistrationState.OPEN)
                .maxParticipants(100)
                .currentParticipants(0)
                .pendingParticipants(0)
                .approvedParticipants(0)
                .theNumberOfCtxhDay(1.0)
                .address("HCMUT")
                .imageUrl("http://example.com/image.jpg")
                .enrollments(new ArrayList<>())
                .build();

        // Create test enrollment
        testEnrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(testStudent)
                .activity(testActivity)
                .status(EnrollmentStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .enrollmentDate(LocalDateTime.now())
                .isCompleted(false)
                .build();
    }

    // ==================== ENROLL IN ACTIVITY TESTS ====================

    @Nested
    @DisplayName("enrollInActivity() Tests")
    class EnrollInActivityTests {

        @Test
        @DisplayName("Should successfully create PENDING enrollment when all conditions are met")
        void enrollInActivity_Success_CreatesPendingEnrollment() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L)).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
            assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
            assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
            assertThat(response.getFullName()).isEqualTo(testStudent.getFullName());
            assertThat(response.getMssv()).isEqualTo(testStudent.getMssv());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getPhoneNumber()).isEqualTo(testStudent.getPhoneNumber());
            assertThat(response.getAcademicYear()).isEqualTo(testStudent.getAcademicYear());
            assertThat(response.getFaculty()).isEqualTo(testStudent.getFaculty());
            assertThat(response.getGender()).isEqualTo(testStudent.getGender());
            assertThat(response.getDateOfBirth()).isEqualTo(testStudent.getDateOfBirth());
            assertThat(response.getTotalCtxhDays()).isEqualTo(testStudent.getTotalCtxhDays());

            verify(studentRepository).findById(1L);
            verify(activityRepository).findById(1L);
            verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
            verify(enrollmentRepository).save(any(Enrollment.class));
            verify(eventPublisher).publishEvent(any(EnrollmentCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw STUDENT_NOT_FOUND when student does not exist")
        void enrollInActivity_ThrowsException_WhenStudentNotFound() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();
            when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(999L, requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

            verify(studentRepository).findById(999L);
            verifyNoInteractions(activityRepository, enrollmentRepository, eventPublisher);
        }

        @Test
        @DisplayName("Should throw ACTIVITY_NOT_FOUND when activity does not exist")
        void enrollInActivity_ThrowsException_WhenActivityNotFound() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(999L)
                    .build();
            when(studentRepository.findById(testStudent.getStudentId())).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(testStudent.getStudentId(), requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

            verify(studentRepository).findById(testStudent.getStudentId());
            verify(activityRepository).findById(999L);
            verify(enrollmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw ALREADY_REGISTRATION when student already enrolled")
        void enrollInActivity_ThrowsException_WhenAlreadyEnrolled() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(testActivity.getActivityId())
                    .build();

            when(studentRepository.findById(testStudent.getStudentId())).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(testActivity.getActivityId())).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(testStudent.getStudentId(), testActivity.getActivityId()))
                    .thenReturn(Optional.of(testEnrollment));

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(testStudent.getStudentId(), requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REGISTRATION);

            verify(studentRepository).findById(testStudent.getStudentId());
            verify(activityRepository).findById(testActivity.getActivityId());
            verify(enrollmentRepository).findByStudentIdAndActivityId(testStudent.getStudentId(), testActivity.getActivityId());
            verify(enrollmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw ACTIVITY_MAX_PENDING_REACHED when activity.canRegister() returns false")
        void enrollInActivity_ThrowsException_WhenMaxPendingReached() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            // Set activity to not allow registration (registration closed)
            testActivity.setRegistrationState(RegistrationState.CLOSED);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(1L, requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_OPEN_FOR_ENROLLMENT);

            verify(studentRepository).findById(1L);
            verify(activityRepository).findById(1L);
            verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
            verify(enrollmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception when registration deadline has passed")
        void enrollInActivity_ThrowsException_WhenRegistrationDeadlinePassed() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            testActivity.setRegistrationDeadline(LocalDateTime.now().minusDays(1));

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(1L, requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_REGISTRATION_DEADLINE_PASSED);

            verify(enrollmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception when activity is already full")
        void enrollInActivity_ThrowsException_WhenActivityFull() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            testActivity.setApprovedParticipants(100);
            testActivity.setMaxParticipants(100);

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.enrollInActivity(1L, requestDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_FULL);

            verify(enrollmentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should call activity helper methods correctly")
        void enrollInActivity_CallsActivityHelperMethods() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(testActivity.getEnrollments()).hasSize(1);
            assertThat(testStudent.getEnrollments()).hasSize(1);
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should publish EnrollmentCreatedEvent after successful enrollment")
        void enrollInActivity_PublishesEvent() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            ArgumentCaptor<EnrollmentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EnrollmentCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            EnrollmentCreatedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEnrollment()).isEqualTo(testEnrollment);
        }

        @Test
        @DisplayName("Should set enrollment fields correctly")
        void enrollInActivity_SetsEnrollmentFieldsCorrectly() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());

            ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
            when(enrollmentRepository.save(enrollmentCaptor.capture())).thenReturn(testEnrollment);

            // Act
            enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            Enrollment savedEnrollment = enrollmentCaptor.getValue();
            assertThat(savedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
            assertThat(savedEnrollment.getStudent()).isEqualTo(testStudent);
            assertThat(savedEnrollment.getActivity()).isEqualTo(testActivity);
            assertThat(savedEnrollment.getAppliedAt()).isNotNull();
            assertThat(savedEnrollment.getEnrollmentDate()).isNotNull();
        }
    }

    // ==================== GET MY REQUESTS TESTS ====================

    @Nested
    @DisplayName("getMyRequests() Tests")
    class GetMyRequestsTests {

        @Test
        @DisplayName("Should successfully return all enrollments")
        void getMyRequests_Success_ReturnsAllEnrollments() {
            // Arrange
            Enrollment enrollment2 = Enrollment.builder()
                    .enrollmentId(2L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.APPROVED)
                    .appliedAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            List<Enrollment> enrollments = List.of(testEnrollment, enrollment2);

            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(enrollments);

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
            assertThat(result.get(0).getEnrollmentStatus()).isEqualTo(EnrollmentStatus.PENDING);
            assertThat(result.get(1).getEnrollmentId()).isEqualTo(enrollment2.getEnrollmentId());
            assertThat(result.get(1).getEnrollmentStatus()).isEqualTo(EnrollmentStatus.APPROVED);

            verify(studentRepository).existsById(testStudent.getStudentId());
            verify(enrollmentRepository).findByStudentId(testStudent.getStudentId());
        }

        @Test
        @DisplayName("Should throw STUDENT_NOT_FOUND when student does not exist")
        void getMyRequests_ThrowsException_WhenStudentNotFound() {
            // Arrange
            when(studentRepository.existsById(anyLong())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.getMyRequests(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

            verify(studentRepository).existsById(999L);
            verifyNoInteractions(enrollmentRepository);
        }

        @Test
        @DisplayName("Should return empty list when no enrollments exist")
        void getMyRequests_ReturnsEmptyList_WhenNoEnrollments() {
            // Arrange
            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(List.of());

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).isEmpty();
            verify(studentRepository).existsById(testStudent.getStudentId());
            verify(enrollmentRepository).findByStudentId(testStudent.getStudentId());
        }

        @Test
        @DisplayName("Should return enrollments with all statuses (PENDING, APPROVED, REJECTED)")
        void getMyRequests_ReturnsAllStatuses() {
            // Arrange
            Enrollment pendingEnrollment = Enrollment.builder()
                    .enrollmentId(1L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.PENDING)
                    .appliedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            Enrollment approvedEnrollment = Enrollment.builder()
                    .enrollmentId(2L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.APPROVED)
                    .appliedAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            Enrollment rejectedEnrollment = Enrollment.builder()
                    .enrollmentId(3L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.REJECTED)
                    .appliedAt(LocalDateTime.now())
                    .rejectedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            List<Enrollment> enrollments = List.of(pendingEnrollment, approvedEnrollment, rejectedEnrollment);

            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(enrollments);

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).extracting(MyActivityResponseDto::getEnrollmentStatus)
                    .containsExactly(EnrollmentStatus.PENDING, EnrollmentStatus.APPROVED, EnrollmentStatus.REJECTED);
        }

        @Test
        @DisplayName("Should map activity details correctly")
        void getMyRequests_MapsActivityDetailsCorrectly() {
            // Arrange
            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId()))
                    .thenReturn(List.of(testEnrollment));

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(1);
            MyActivityResponseDto dto = result.get(0);
            assertThat(dto.getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
            assertThat(dto.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.PENDING);
            assertThat(dto.getActivityId()).isEqualTo(testActivity.getActivityId());
            assertThat(dto.getActivityTitle()).isEqualTo(testActivity.getTitle());
            assertThat(dto.getShortDescription()).isEqualTo(testActivity.getShortDescription());
            assertThat(dto.getCategory()).isEqualTo(testActivity.getCategory());
            assertThat(dto.getRegistrationState()).isEqualTo(testActivity.getRegistrationState());
            assertThat(dto.getStartDateTime()).isEqualTo(testActivity.getStartDateTime());
            assertThat(dto.getEndDateTime()).isEqualTo(testActivity.getEndDateTime());
            assertThat(dto.getAddress()).isEqualTo(testActivity.getAddress());
            assertThat(dto.getBenefitsCtxh()).isEqualTo(testActivity.getTheNumberOfCtxhDay());
            assertThat(dto.getOrganizationId()).isEqualTo(testOrganization.getOrganizationId());
            assertThat(dto.getOrganizationName()).isEqualTo(testOrganization.getOrganizationName());
            assertThat(dto.getActivityImage()).isEqualTo(testActivity.getImageUrl());
        }

        @Test
        @DisplayName("Should handle completed enrollments correctly")
        void getMyRequests_HandlesCompletedEnrollments() {
            // Arrange
            LocalDateTime completedTime = LocalDateTime.now().minusDays(1);
            testEnrollment.setIsCompleted(true);
            testEnrollment.setCompletedAt(completedTime);

            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId()))
                    .thenReturn(List.of(testEnrollment));

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(1);
            MyActivityResponseDto dto = result.get(0);
            assertThat(dto.getIsCompleted()).isTrue();
            assertThat(dto.getCompletedAt()).isEqualTo(completedTime);
        }
    }

    // ==================== GET MY ACTIVITIES TESTS ====================

    @Nested
    @DisplayName("getMyActivities() Tests")
    class GetMyActivitiesTests {

        @Test
        @DisplayName("Should successfully return only APPROVED enrollments")
        void getMyActivities_Success_ReturnsOnlyApprovedEnrollments() {
            // Arrange
            Enrollment approvedEnrollment1 = Enrollment.builder()
                    .enrollmentId(1L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.APPROVED)
                    .appliedAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            Activity activity2 = Activity.builder()
                    .activityId(2L)
                    .title("Second Activity")
                    .shortDescription("Second Description")
                    .organization(testOrganization)
                    .category(ActivityCategory.EDUCATION_SUPPORT)
                    .startDateTime(LocalDateTime.now().plusDays(10))
                    .endDateTime(LocalDateTime.now().plusDays(11))
                    .registrationState(RegistrationState.OPEN)
                    .maxParticipants(50)
                    .theNumberOfCtxhDay(2.0)
                    .address("Another Location")
                    .build();

            Enrollment approvedEnrollment2 = Enrollment.builder()
                    .enrollmentId(2L)
                    .student(testStudent)
                    .activity(activity2)
                    .status(EnrollmentStatus.APPROVED)
                    .appliedAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            List<Enrollment> approvedEnrollments = List.of(approvedEnrollment1, approvedEnrollment2);

            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
                    .thenReturn(approvedEnrollments);

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(dto -> dto.getEnrollmentStatus() == EnrollmentStatus.APPROVED);
            assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());
            assertThat(result.get(0).getActivityTitle()).isEqualTo(testActivity.getTitle());
            assertThat(result.get(1).getActivityId()).isEqualTo(activity2.getActivityId());
            assertThat(result.get(1).getActivityTitle()).isEqualTo(activity2.getTitle());

            verify(studentRepository).existsById(testStudent.getStudentId());
            verify(enrollmentRepository).findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED);
        }

        @Test
        @DisplayName("Should throw STUDENT_NOT_FOUND when student does not exist")
        void getMyActivities_ThrowsException_WhenStudentNotFound() {
            // Arrange
            when(studentRepository.existsById(anyLong())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.getMyActivities(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

            verify(studentRepository).existsById(999L);
            verifyNoInteractions(enrollmentRepository);
        }

        @Test
        @DisplayName("Should return empty list when no approved enrollments exist")
        void getMyActivities_ReturnsEmptyList_WhenNoApprovedEnrollments() {
            // Arrange
            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
                    .thenReturn(List.of());

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());

            // Assert
            assertThat(result).isEmpty();
            verify(studentRepository).existsById(testStudent.getStudentId());
            verify(enrollmentRepository).findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED);
        }

        @Test
        @DisplayName("Should map activity details correctly")
        void getMyActivities_MapsActivityDetailsCorrectly() {
            // Arrange
            Enrollment approvedEnrollment = Enrollment.builder()
                    .enrollmentId(1L)
                    .student(testStudent)
                    .activity(testActivity)
                    .status(EnrollmentStatus.APPROVED)
                    .appliedAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .enrollmentDate(LocalDateTime.now())
                    .isCompleted(false)
                    .build();

            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
                    .thenReturn(List.of(approvedEnrollment));

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(1);
            MyActivityResponseDto dto = result.get(0);
            assertThat(dto.getActivityTitle()).isEqualTo(testActivity.getTitle());
            assertThat(dto.getShortDescription()).isEqualTo(testActivity.getShortDescription());
            assertThat(dto.getOrganizationName()).isEqualTo(testOrganization.getOrganizationName());
            assertThat(dto.getBenefitsCtxh()).isEqualTo(testActivity.getTheNumberOfCtxhDay());
            assertThat(dto.getCategory()).isEqualTo(testActivity.getCategory());
            assertThat(dto.getRegistrationState()).isEqualTo(testActivity.getRegistrationState());
        }

        @Test
        @DisplayName("Should not return PENDING or REJECTED enrollments")
        void getMyActivities_DoesNotReturnPendingOrRejected() {
            // Arrange
            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
                    .thenReturn(List.of());

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());

            // Assert
            assertThat(result).isEmpty();
            verify(enrollmentRepository).findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED);
        }
    }

    // ==================== CANCEL ENROLLMENT TESTS ====================

    @Nested
    @DisplayName("cancelEnrollment() Tests")
    class CancelEnrollmentTests {

        @Test
        @DisplayName("Should successfully cancel PENDING enrollment")
        void cancelEnrollment_Success_CancelsPendingEnrollment() {
            // Arrange
            testActivity.setPendingParticipants(1);
            testActivity.setCurrentParticipants(1);
            testEnrollment.setStatus(EnrollmentStatus.PENDING);

            when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(testEnrollment));
            doNothing().when(enrollmentRepository).delete(testEnrollment);

            // Act
            enrollmentService.cancelEnrollment(1L, 1L);

            // Assert
            verify(enrollmentRepository).findByIdAndStudentId(1L, 1L);
            verify(enrollmentRepository).delete(testEnrollment);

            // Verify that the activity counts were updated
            assertThat(testActivity.getPendingParticipants()).isEqualTo(0);
            assertThat(testActivity.getCurrentParticipants()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw ENROLLMENT_NOT_FOUND when enrollment does not exist")
        void cancelEnrollment_ThrowsException_WhenEnrollmentNotFound() {
            // Arrange
            when(enrollmentRepository.findByIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(testStudent.getStudentId(), 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_FOUND);

            verify(enrollmentRepository).findByIdAndStudentId(999L, testStudent.getStudentId());
            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ENROLLMENT_NOT_FOUND when enrollment belongs to different student")
        void cancelEnrollment_ThrowsException_WhenNotOwnedByStudent() {
            // Arrange
            Long differentStudentId = 999L;
            when(enrollmentRepository.findByIdAndStudentId(testEnrollment.getEnrollmentId(), differentStudentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(differentStudentId, testEnrollment.getEnrollmentId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_FOUND);

            verify(enrollmentRepository).findByIdAndStudentId(testEnrollment.getEnrollmentId(), differentStudentId);
            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ENROLLMENT_CANNOT_BE_CANCELLED when enrollment cannot be cancelled")
        void cancelEnrollment_ThrowsException_WhenCannotBeCancelled() {
            // Arrange
            // Make enrollment not cancellable by changing status to APPROVED
            testEnrollment.setStatus(EnrollmentStatus.APPROVED);

            when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(testEnrollment));

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);

            verify(enrollmentRepository).findByIdAndStudentId(1L, 1L);
            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when activity has already started")
        void cancelEnrollment_ThrowsException_WhenActivityStarted() {
            // Arrange
            testActivity.setStartDateTime(LocalDateTime.now().minusDays(1));
            testEnrollment.setStatus(EnrollmentStatus.PENDING);

            when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(testEnrollment));

            // Act & Assert
            assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);

            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should update activity and student relationships correctly")
        void cancelEnrollment_UpdatesRelationshipsCorrectly() {
            // Arrange
            testActivity.setPendingParticipants(5);
            testActivity.setCurrentParticipants(5);
            testEnrollment.setStatus(EnrollmentStatus.PENDING);
            testStudent.addEnrollment(testEnrollment);
            testActivity.addEnrollment(testEnrollment);

            when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(testEnrollment));
            doNothing().when(enrollmentRepository).delete(testEnrollment);

            int initialPendingCount = testActivity.getPendingParticipants();
            int initialCurrentCount = testActivity.getCurrentParticipants();

            // Act
            enrollmentService.cancelEnrollment(1L, 1L);

            // Assert
            verify(enrollmentRepository).delete(testEnrollment);
            assertThat(testActivity.getPendingParticipants()).isEqualTo(initialPendingCount - 1);
            assertThat(testActivity.getCurrentParticipants()).isEqualTo(initialCurrentCount - 1);
            assertThat(testStudent.getEnrollments()).doesNotContain(testEnrollment);
            assertThat(testActivity.getEnrollments()).doesNotContain(testEnrollment);
        }

        @Test
        @DisplayName("Should handle edge case with zero pending participants")
        void cancelEnrollment_HandlesZeroPendingParticipants() {
            // Arrange
            testActivity.setPendingParticipants(0);
            testActivity.setCurrentParticipants(0);
            testEnrollment.setStatus(EnrollmentStatus.PENDING);

            when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(testEnrollment));
            doNothing().when(enrollmentRepository).delete(testEnrollment);

            // Act
            enrollmentService.cancelEnrollment(1L, 1L);

            // Assert
            verify(enrollmentRepository).delete(testEnrollment);
            // Should not go negative
            assertThat(testActivity.getPendingParticipants()).isGreaterThanOrEqualTo(0);
            assertThat(testActivity.getCurrentParticipants()).isGreaterThanOrEqualTo(0);
        }
    }

    // ==================== MAPPING TESTS ====================

    @Nested
    @DisplayName("Mapping Methods Tests")
    class MappingTests {

        @Test
        @DisplayName("Should map enrollment to EnrollmentResponseDto with all fields")
        void mapToEnrollmentResponseDto_MapsAllFields() {
            // Arrange
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response.getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
            assertThat(response.getStatus()).isEqualTo(testEnrollment.getStatus());
            assertThat(response.getAppliedAt()).isEqualTo(testEnrollment.getAppliedAt());
            assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
            assertThat(response.getFullName()).isEqualTo(testStudent.getFullName());
            assertThat(response.getMssv()).isEqualTo(testStudent.getMssv());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getPhoneNumber()).isEqualTo(testStudent.getPhoneNumber());
        }

        @Test
        @DisplayName("Should map enrollment to MyActivityResponseDto with all activity details")
        void mapToMyActivityResponseDto_MapsAllActivityDetails() {
            // Arrange
            when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
            when(enrollmentRepository.findByStudentId(testStudent.getStudentId()))
                    .thenReturn(List.of(testEnrollment));

            // Act
            List<MyActivityResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());

            // Assert
            assertThat(result).hasSize(1);
            MyActivityResponseDto dto = result.get(0);
            assertThat(dto.getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
            assertThat(dto.getEnrollmentStatus()).isEqualTo(testEnrollment.getStatus());
            assertThat(dto.getAppliedAt()).isEqualTo(testEnrollment.getAppliedAt());
            assertThat(dto.getActivityId()).isEqualTo(testActivity.getActivityId());
            assertThat(dto.getActivityTitle()).isEqualTo(testActivity.getTitle());
            assertThat(dto.getShortDescription()).isEqualTo(testActivity.getShortDescription());
            assertThat(dto.getCategory()).isEqualTo(testActivity.getCategory());
            assertThat(dto.getOrganizationId()).isEqualTo(testOrganization.getOrganizationId());
            assertThat(dto.getOrganizationName()).isEqualTo(testOrganization.getOrganizationName());
        }
    }

    // ==================== EDGE CASES AND BOUNDARY TESTS ====================

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle multiple concurrent enrollments for same student")
        void enrollInActivity_HandlesMultipleEnrollments() {
            // Arrange
            Activity activity2 = Activity.builder()
                    .activityId(2L)
                    .title("Second Activity")
                    .organization(testOrganization)
                    .startDateTime(LocalDateTime.now().plusDays(7))
                    .endDateTime(LocalDateTime.now().plusDays(8))
                    .registrationState(RegistrationState.OPEN)
                    .maxParticipants(100)
                    .currentParticipants(0)
                    .pendingParticipants(0)
                    .approvedParticipants(0)
                    .theNumberOfCtxhDay(1.0)
                    .enrollments(new ArrayList<>())
                    .build();

            EnrollmentRequestDto requestDto1 = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            EnrollmentRequestDto requestDto2 = EnrollmentRequestDto.builder()
                    .activityId(2L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(activityRepository.findById(2L)).thenReturn(Optional.of(activity2));
            when(enrollmentRepository.findByStudentIdAndActivityId(1L, 1L)).thenReturn(Optional.empty());
            when(enrollmentRepository.findByStudentIdAndActivityId(1L, 2L)).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response1 = enrollmentService.enrollInActivity(1L, requestDto1);
            EnrollmentResponseDto response2 = enrollmentService.enrollInActivity(1L, requestDto2);

            // Assert
            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should handle student with zero CTXH days")
        void enrollInActivity_HandlesZeroCtxhDays() {
            // Arrange
            testStudent.setTotalCtxhDays(0.0);
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalCtxhDays()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle activity with very high max participants (unlimited-like)")
        void enrollInActivity_HandlesVeryHighMaxParticipants() {
            // Arrange
            testActivity.setMaxParticipants(Integer.MAX_VALUE);
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response).isNotNull();
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should handle activity at registration deadline boundary")
        void enrollInActivity_HandlesRegistrationDeadlineBoundary() {
            // Arrange - Set deadline to exactly now
            testActivity.setRegistrationDeadline(LocalDateTime.now().plusSeconds(1));
            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should handle very large participant numbers")
        void enrollInActivity_HandlesLargeParticipantNumbers() {
            // Arrange
            testActivity.setMaxParticipants(10000);
            testActivity.setCurrentParticipants(9999);
            testActivity.setApprovedParticipants(5000);

            EnrollmentRequestDto requestDto = EnrollmentRequestDto.builder()
                    .activityId(1L)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
            when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
            when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

            // Act
            EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);

            // Assert
            assertThat(response).isNotNull();
            verify(enrollmentRepository).save(any(Enrollment.class));
        }
    }
}
