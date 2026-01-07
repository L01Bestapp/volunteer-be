package com.ctxh.volunteer.module.activity.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.activity.specification.ActivitySpecification;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.repository.OrganizationRepository;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityService Unit Tests")
class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private Organization testOrganization;
    private Activity testActivity;
    private CreateActivityRequestDto createRequest;
    private UpdateActivityRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        // Create test organization
        testOrganization = Organization.builder()
                .organizationId(1L)
                .organizationName("Test Organization")
                .build();

        // Create test activity
        testActivity = Activity.builder()
                .activityId(1L)
                .organization(testOrganization)
                .title("Test Activity")
                .description("Test Description")
                .shortDescription("Short Desc")
                .category(ActivityCategory.EDUCATION_SUPPORT)
                .startDateTime(LocalDateTime.now().plusDays(7))
                .endDateTime(LocalDateTime.now().plusDays(8))
                .registrationOpensAt(LocalDateTime.now())
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .maxParticipants(100)
                .currentParticipants(0)
                .pendingParticipants(0)
                .approvedParticipants(0)
                .theNumberOfCtxhDay(1.0)
                .status(ActivityStatus.OPEN)
                .build();

        // Create request DTOs
        createRequest = new CreateActivityRequestDto();
        createRequest.setTitle("New Activity");
        createRequest.setDescription("New Description");
        createRequest.setShortDescription("Short Description");
        createRequest.setCategory("EDUCATION_SUPPORT");
        createRequest.setStartDateTime(LocalDateTime.now().plusDays(7));
        createRequest.setEndDateTime(LocalDateTime.now().plusDays(8));
        createRequest.setRegistrationDeadline(LocalDateTime.now().plusDays(5));
        createRequest.setMaxParticipants(100);
        createRequest.setTheNumberOfCtxhDay(1.0);

        updateRequest = new UpdateActivityRequestDto();
        updateRequest.setName("Updated Activity");
        updateRequest.setDescription("Updated Description");
    }

    // ==================== CREATE ACTIVITY TESTS ====================

    @Test
    @DisplayName("Create Activity - Success creates new activity")
    void createActivity_Success_CreatesNewActivity() {
        // Arrange
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        // Act
        ActivityResponseDto response = activityService.createActivity(1L, createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActivityId()).isEqualTo(testActivity.getActivityId());
        assertThat(response.getOrganizationId()).isEqualTo(testOrganization.getOrganizationId());

        verify(organizationRepository).findById(1L);
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Create Activity - Fails when organization not found")
    void createActivity_ThrowsException_WhenOrganizationNotFound() {
        // Arrange
        when(organizationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.createActivity(999L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_NOT_FOUND);

        verify(organizationRepository).findById(999L);
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Activity - Fails when end date before start date")
    void createActivity_ThrowsException_WhenEndDateBeforeStartDate() {
        // Arrange
        createRequest.setEndDateTime(LocalDateTime.now().plusDays(1));
        createRequest.setStartDateTime(LocalDateTime.now().plusDays(7)); // Start after end

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));

        // Act & Assert
        assertThatThrownBy(() -> activityService.createActivity(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACTIVITY_DATE);

        verify(organizationRepository).findById(1L);
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Activity - Fails when registration deadline is in the past")
    void createActivity_ThrowsException_WhenRegistrationDeadlineInPast() {
        // Arrange
        createRequest.setRegistrationDeadline(LocalDateTime.now().minusDays(1)); // Past deadline

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));

        // Act & Assert
        assertThatThrownBy(() -> activityService.createActivity(1L, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REGISTRATION_DEADLINE);

        verify(organizationRepository).findById(1L);
        verify(activityRepository, never()).save(any());
    }

    // ==================== GET ACTIVITIES BY ORGANIZATION TESTS ====================

    @Test
    @DisplayName("Get Activities By Organization - Success returns all activities")
    void getActivitiesByOrganization_Success_ReturnsAllActivities() {
        // Arrange
        Activity activity2 = Activity.builder()
                .activityId(2L)
                .organization(testOrganization)
                .title("Activity 2")
                .startDateTime(LocalDateTime.now().plusDays(10))
                .endDateTime(LocalDateTime.now().plusDays(11))
                .build();

        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(activityRepository.findByOrganizationId(1L)).thenReturn(List.of(testActivity, activity2));

        // Act
        List<ActivityListResponseDto> result = activityService.getActivitiesByOrganization(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());
        assertThat(result.get(1).getActivityId()).isEqualTo(activity2.getActivityId());

        verify(organizationRepository).existsById(1L);
        verify(activityRepository).findByOrganizationId(1L);
    }

    @Test
    @DisplayName("Get Activities By Organization - Fails when organization not found")
    void getActivitiesByOrganization_ThrowsException_WhenOrganizationNotFound() {
        // Arrange
        when(organizationRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> activityService.getActivitiesByOrganization(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_NOT_FOUND);

        verify(organizationRepository).existsById(999L);
        verify(activityRepository, never()).findByOrganizationId(anyLong());
    }

    // ==================== GET ACTIVITY BY ID TESTS ====================

    @Test
    @DisplayName("Get Activity By ID - Success returns activity")
    void getActivityById_Success_ReturnsActivity() {
        // Arrange
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // Act
        ActivityResponseDto response = activityService.getActivityById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActivityId()).isEqualTo(testActivity.getActivityId());
        assertThat(response.getName()).isEqualTo(testActivity.getTitle());

        verify(activityRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Activity By ID - Fails when activity not found")
    void getActivityById_ThrowsException_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.getActivityById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findById(999L);
    }

    // ==================== UPDATE ACTIVITY TESTS ====================

    @Test
    @DisplayName("Update Activity - Success updates activity")
    void updateActivity_Success_UpdatesActivity() {
        // Arrange
        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);

        // Act
        ActivityResponseDto response = activityService.updateActivity(1L, 1L, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository).save(testActivity);
    }

    @Test
    @DisplayName("Update Activity - Fails when activity not found")
    void updateActivity_ThrowsException_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findByIdAndOrganizationId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.updateActivity(1L, 999L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findByIdAndOrganizationId(999L, 1L);
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Activity - Fails when activity already completed")
    void updateActivity_ThrowsException_WhenActivityCompleted() {
        // Arrange
        testActivity.setStatus(ActivityStatus.COMPLETED);
        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));

        // Act & Assert
        assertThatThrownBy(() -> activityService.updateActivity(1L, 1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_ALREADY_COMPLETED);

        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Activity - Fails when dates are invalid after update")
    void updateActivity_ThrowsException_WhenDatesInvalidAfterUpdate() {
        // Arrange
        updateRequest.setStartDateTime(LocalDateTime.now().plusDays(10));
        updateRequest.setEndDateTime(LocalDateTime.now().plusDays(1)); // End before start

        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));

        // Act & Assert
        assertThatThrownBy(() -> activityService.updateActivity(1L, 1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACTIVITY_DATE);

        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository, never()).save(any());
    }

    // ==================== DELETE ACTIVITY TESTS ====================

    @Test
    @DisplayName("Delete Activity - Success deletes activity")
    void deleteActivity_Success_DeletesActivity() {
        // Arrange
        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));
        doNothing().when(activityRepository).delete(testActivity);

        // Act
        activityService.deleteActivity(1L, 1L);

        // Assert
        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository).delete(testActivity);
    }

    @Test
    @DisplayName("Delete Activity - Fails when activity not found")
    void deleteActivity_ThrowsException_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findByIdAndOrganizationId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.deleteActivity(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findByIdAndOrganizationId(999L, 1L);
        verify(activityRepository, never()).delete(any(Activity.class));
    }

    @Test
    @DisplayName("Delete Activity - Fails when activity already completed")
    void deleteActivity_ThrowsException_WhenActivityCompleted() {
        // Arrange
        testActivity.setStatus(ActivityStatus.COMPLETED);
        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));

        // Act & Assert
        assertThatThrownBy(() -> activityService.deleteActivity(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_ALREADY_COMPLETED);

        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository, never()).delete(any(Activity.class));
    }

    @Test
    @DisplayName("Delete Activity - Fails when activity already started")
    void deleteActivity_ThrowsException_WhenActivityStarted() {
        // Arrange
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getStatus()).thenReturn(ActivityStatus.OPEN);
        when(mockActivity.hasStarted()).thenReturn(true);

        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(mockActivity));

        // Act & Assert
        assertThatThrownBy(() -> activityService.deleteActivity(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_ALREADY_STARTED);

        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository, never()).delete(any(Activity.class));
    }

    // ==================== CLOSE REGISTRATION TESTS ====================

    @Test
    @DisplayName("Close Registration - Success closes registration")
    void closeRegistration_Success_ClosesRegistration() {
        // Arrange
        Activity mockActivity = mock(Activity.class);
        when(mockActivity.getStatus()).thenReturn(ActivityStatus.OPEN);
        when(mockActivity.getActivityId()).thenReturn(1L);
        when(mockActivity.getOrganization()).thenReturn(testOrganization);

        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(mockActivity));
        when(activityRepository.save(mockActivity)).thenReturn(mockActivity);

        // Act
        activityService.closeActivityRegistration(1L, 1L);

        // Assert
        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(mockActivity).closeRegistration();
        verify(activityRepository).save(mockActivity);
    }

    @Test
    @DisplayName("Close Registration - Fails when activity not found")
    void closeRegistration_ThrowsException_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.findByIdAndOrganizationId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.closeActivityRegistration(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).findByIdAndOrganizationId(999L, 1L);
    }

    @Test
    @DisplayName("Close Registration - Fails when activity not open or full")
    void closeRegistration_ThrowsException_WhenActivityNotOpenOrFull() {
        // Arrange
        testActivity.setStatus(ActivityStatus.CLOSED);
        when(activityRepository.findByIdAndOrganizationId(1L, 1L)).thenReturn(Optional.of(testActivity));

        // Act & Assert
        assertThatThrownBy(() -> activityService.closeActivityRegistration(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_OPEN);

        verify(activityRepository).findByIdAndOrganizationId(1L, 1L);
        verify(activityRepository, never()).save(any());
    }

    // ==================== GET ENROLLMENTS TESTS ====================

    @Test
    @DisplayName("Get Activity Enrollments - Success returns all enrollments")
    void getActivityEnrollments_Success_ReturnsAllEnrollments() {
        // Arrange
        User user = User.builder().userId(1L).email("student@hcmut.edu.vn").build();
        Student student = Student.builder().studentId(1L).user(user).fullName("Test Student").build();
        Enrollment enrollment = Enrollment.builder()
                .enrollmentId(1L)
                .student(student)
                .activity(testActivity)
                .status(EnrollmentStatus.PENDING)
                .build();

        when(activityRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findByActivityId(1L)).thenReturn(List.of(enrollment));

        // Act
        List<EnrollmentResponseDto> result = activityService.getActivityEnrollments(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnrollmentId()).isEqualTo(enrollment.getEnrollmentId());

        verify(activityRepository).existsById(1L);
        verify(enrollmentRepository).findByActivityId(1L);
    }

    @Test
    @DisplayName("Get Activity Enrollments - Fails when activity not found")
    void getActivityEnrollments_ThrowsException_WhenActivityNotFound() {
        // Arrange
        when(activityRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> activityService.getActivityEnrollments(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);

        verify(activityRepository).existsById(999L);
        verify(enrollmentRepository, never()).findByActivityId(anyLong());
    }

    // ==================== APPROVE/REJECT ENROLLMENT TESTS ====================

    @Test
    @DisplayName("Approve Enrollment - Success approves pending enrollment")
    void approveEnrollment_Success_ApprovesPendingEnrollment() {
        // Arrange
        Enrollment mockEnrollment = mock(Enrollment.class);
        User user = User.builder().userId(1L).email("student@hcmut.edu.vn").build();
        Student student = Student.builder().studentId(1L).user(user).fullName("Test Student").build();

        when(mockEnrollment.getStatus()).thenReturn(EnrollmentStatus.PENDING);
        when(mockEnrollment.getStudent()).thenReturn(student);
        when(enrollmentRepository.findByIdAndActivityId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));
        when(enrollmentRepository.save(mockEnrollment)).thenReturn(mockEnrollment);

        // Act
        activityService.approveEnrollment(1L, 1L, 100L);

        // Assert
        verify(enrollmentRepository).findByIdAndActivityId(1L, 1L);
        verify(mockEnrollment).approve(100L);
        verify(enrollmentRepository).save(mockEnrollment);
    }

    @Test
    @DisplayName("Approve Enrollment - Fails when enrollment not found")
    void approveEnrollment_ThrowsException_WhenEnrollmentNotFound() {
        // Arrange
        when(enrollmentRepository.findByIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> activityService.approveEnrollment(1L, 999L, 100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_FOUND);

        verify(enrollmentRepository).findByIdAndActivityId(999L, 1L);
    }

    @Test
    @DisplayName("Approve Enrollment - Fails when enrollment not pending")
    void approveEnrollment_ThrowsException_WhenEnrollmentNotPending() {
        // Arrange
        Enrollment mockEnrollment = mock(Enrollment.class);
        when(mockEnrollment.getStatus()).thenReturn(EnrollmentStatus.APPROVED);
        when(enrollmentRepository.findByIdAndActivityId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));

        // Act & Assert
        assertThatThrownBy(() -> activityService.approveEnrollment(1L, 1L, 100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_PENDING);

        verify(enrollmentRepository).findByIdAndActivityId(1L, 1L);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reject Enrollment - Success rejects pending enrollment")
    void rejectEnrollment_Success_RejectsPendingEnrollment() {
        // Arrange
        Enrollment mockEnrollment = mock(Enrollment.class);
        User user = User.builder().userId(1L).email("student@hcmut.edu.vn").build();
        Student student = Student.builder().studentId(1L).user(user).fullName("Test Student").build();

        when(mockEnrollment.getStatus()).thenReturn(EnrollmentStatus.PENDING);
        when(mockEnrollment.getStudent()).thenReturn(student);
        when(enrollmentRepository.findByIdAndActivityId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));
        when(enrollmentRepository.save(mockEnrollment)).thenReturn(mockEnrollment);

        // Act
        activityService.rejectEnrollment(1L, 1L, 100L);

        // Assert
        verify(enrollmentRepository).findByIdAndActivityId(1L, 1L);
        verify(mockEnrollment).reject(100L);
        verify(enrollmentRepository).save(mockEnrollment);
    }

    // ==================== STUDENT DISCOVERY TESTS ====================

    @Test
    @DisplayName("Get Available Activities - Success returns available activities")
    void getAvailableActivities_Success_ReturnsAvailableActivities() {
        // Arrange
        when(activityRepository.findAvailableActivities(any(LocalDateTime.class)))
                .thenReturn(List.of(testActivity));

        // Act
        List<ActivityListResponseDto> result = activityService.getAvailableActivities();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());

        verify(activityRepository).findAvailableActivities(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Search Activities - Success with keyword")
    void searchActivities_Success_WithKeyword() {
        // Arrange
        when(activityRepository.searchByKeyword("test")).thenReturn(List.of(testActivity));

        // Act
        List<ActivityListResponseDto> result = activityService.searchActivities("test");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());

        verify(activityRepository).searchByKeyword("test");
    }

    @Test
    @DisplayName("Search Activities - Returns all when keyword is empty")
    void searchActivities_ReturnsAll_WhenKeywordEmpty() {
        // Arrange
        when(activityRepository.findAvailableActivities(any(LocalDateTime.class)))
                .thenReturn(List.of(testActivity));

        // Act
        List<ActivityListResponseDto> result = activityService.searchActivities("");

        // Assert
        assertThat(result).hasSize(1);
        verify(activityRepository).findAvailableActivities(any(LocalDateTime.class));
        verify(activityRepository, never()).searchByKeyword(anyString());
    }

    @Test
    @DisplayName("Search Activities Advanced - Success with filters")
    void searchActivitiesAdvanced_Success_WithFilters() {
        // Arrange
        when(activityRepository.findAll(any(Specification.class))).thenReturn(List.of(testActivity));

        // Act
        List<ActivityListResponseDto> result = activityService.searchActivitiesAdvanced(
                "test",
                ActivityCategory.EDUCATION_SUPPORT,
                ActivityStatus.OPEN,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());

        verify(activityRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Get Activity Detail - Success returns activity detail")
    void getActivityDetail_Success_ReturnsActivityDetail() {
        // Arrange
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // Act
        ActivityResponseDto response = activityService.getActivityDetail(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getActivityId()).isEqualTo(testActivity.getActivityId());

        verify(activityRepository).findById(1L);
    }
}
