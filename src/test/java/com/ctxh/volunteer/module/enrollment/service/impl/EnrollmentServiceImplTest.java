//package com.ctxh.volunteer.module.enrollment.service.impl;
//
//import com.ctxh.volunteer.common.exception.BusinessException;
//import com.ctxh.volunteer.common.exception.ErrorCode;
//import com.ctxh.volunteer.module.activity.entity.Activity;
//import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
//import com.ctxh.volunteer.module.auth.entity.User;
//import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
//import com.ctxh.volunteer.module.enrollment.dto.EnrollmentRequestDto;
//import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
//import com.ctxh.volunteer.module.enrollment.dto.MyActivityResponseDto;
//import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
//import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
//import com.ctxh.volunteer.module.organization.entity.Organization;
//import com.ctxh.volunteer.module.student.entity.Student;
//import com.ctxh.volunteer.module.student.repository.StudentRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("EnrollmentService Unit Tests")
//class EnrollmentServiceImplTest {
//
//    @Mock
//    private EnrollmentRepository enrollmentRepository;
//
//    @Mock
//    private ActivityRepository activityRepository;
//
//    @Mock
//    private StudentRepository studentRepository;
//
//    @InjectMocks
//    private EnrollmentServiceImpl enrollmentService;
//
//    private Student testStudent;
//    private Activity testActivity;
//    private Organization testOrganization;
//    private User testUser;
//    private Enrollment testEnrollment;
//
//    @BeforeEach
//    void setUp() {
//        // Create test user
//        testUser = User.builder()
//                .userId(1L)
//                .email("student@hcmut.edu.vn")
//                .isVerified(true)
//                .build();
//
//        // Create test organization
//        testOrganization = Organization.builder()
//                .organizationId(1L)
//                .organizationName("Test Organization")
//                .build();
//
//        // Create test student
//        testStudent = Student.builder()
//                .studentId(1L)
//                .user(testUser)
//                .fullName("Test Student")
//                .mssv("2012345")
//                .totalCtxhDays(0.0)
//                .build();
//
//        // Create test activity
//        testActivity = Activity.builder()
//                .activityId(1L)
//                .title("Test Activity")
//                .shortDescription("Test Description")
//                .organization(testOrganization)
//                .startDateTime(LocalDateTime.now().plusDays(7))
//                .endDateTime(LocalDateTime.now().plusDays(8))
//                .maxParticipants(100)
//                .currentParticipants(0)
//                .pendingParticipants(0)
//                .approvedParticipants(0)
//                .theNumberOfCtxhDay(1.0)
//                .build();
//
//        // Create test enrollment
//        testEnrollment = Enrollment.builder()
//                .enrollmentId(1L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.PENDING)
//                .appliedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .isCompleted(false)
//                .build();
//    }
//
//    // ==================== ENROLL IN ACTIVITY TESTS ====================
//
//    @Test
//    @DisplayName("Enroll In Activity - Success creates PENDING enrollment")
//    void enrollInActivity_Success_CreatesPendingEnrollment() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(1L);
//        Activity mockActivity = mock(Activity.class);
//        Student mockStudent = mock(Student.class);
//
//        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
//        when(activityRepository.findById(1L)).thenReturn(Optional.of(mockActivity));
//        when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
//        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);
//        when(mockActivity.canRegister()).thenReturn(true);
//
//        // Act
//        EnrollmentResponseDto response = enrollmentService.enrollInActivity(1L, requestDto);
//
//        // Assert
//        assertThat(response).isNotNull();
//        assertThat(response.getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
//        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
//        assertThat(response.getStudentId()).isEqualTo(testStudent.getStudentId());
//
//        verify(studentRepository).findById(1L);
//        verify(activityRepository).findById(1L);
//        verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
//        verify(enrollmentRepository).save(any(Enrollment.class));
//    }
//
//    @Test
//    @DisplayName("Enroll In Activity - Fails when student not found")
//    void enrollInActivity_ThrowsException_WhenStudentNotFound() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(1L);
//        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.enrollInActivity(999L, requestDto))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
//
//        verify(studentRepository).findById(999L);
//        verifyNoInteractions(activityRepository, enrollmentRepository);
//    }
//
//    @Test
//    @DisplayName("Enroll In Activity - Fails when activity not found")
//    void enrollInActivity_ThrowsException_WhenActivityNotFound() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(999L);
//        when(studentRepository.findById(testStudent.getStudentId())).thenReturn(Optional.of(testStudent));
//        when(activityRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.enrollInActivity(testStudent.getStudentId(), requestDto))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_NOT_FOUND);
//
//        verify(studentRepository).findById(testStudent.getStudentId());
//        verify(activityRepository).findById(999L);
//        verify(enrollmentRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Enroll In Activity - Fails when already enrolled")
//    void enrollInActivity_ThrowsException_WhenAlreadyEnrolled() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(testActivity.getActivityId());
//
//        when(studentRepository.findById(testStudent.getStudentId())).thenReturn(Optional.of(testStudent));
//        when(activityRepository.findById(testActivity.getActivityId())).thenReturn(Optional.of(testActivity));
//        when(enrollmentRepository.findByStudentIdAndActivityId(testStudent.getStudentId(), testActivity.getActivityId()))
//                .thenReturn(Optional.of(testEnrollment));
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.enrollInActivity(testStudent.getStudentId(), requestDto))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ENROLLED);
//
//        verify(studentRepository).findById(testStudent.getStudentId());
//        verify(activityRepository).findById(testActivity.getActivityId());
//        verify(enrollmentRepository).findByStudentIdAndActivityId(testStudent.getStudentId(), testActivity.getActivityId());
//        verify(enrollmentRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Enroll In Activity - Fails when activity max pending reached")
//    void enrollInActivity_ThrowsException_WhenMaxPendingReached() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(1L);
//        Activity mockActivity = mock(Activity.class);
//        Student mockStudent = mock(Student.class);
//
//        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
//        when(activityRepository.findById(1L)).thenReturn(Optional.of(mockActivity));
//        when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
//        when(mockActivity.canRegister()).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.enrollInActivity(1L, requestDto))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTIVITY_MAX_PENDING_REACHED);
//
//        verify(studentRepository).findById(1L);
//        verify(activityRepository).findById(1L);
//        verify(enrollmentRepository).findByStudentIdAndActivityId(1L, 1L);
//        verify(enrollmentRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Enroll In Activity - Calls activity helper methods correctly")
//    void enrollInActivity_CallsActivityHelperMethods() {
//        // Arrange
//        EnrollmentRequestDto requestDto = new EnrollmentRequestDto(1L);
//        Activity mockActivity = mock(Activity.class);
//        Student mockStudent = mock(Student.class);
//
//        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
//        when(activityRepository.findById(1L)).thenReturn(Optional.of(mockActivity));
//        when(enrollmentRepository.findByStudentIdAndActivityId(anyLong(), anyLong())).thenReturn(Optional.empty());
//        when(mockActivity.canRegister()).thenReturn(true);
//        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);
//
//        // Act
//        enrollmentService.enrollInActivity(1L, requestDto);
//
//        // Assert
//        verify(mockActivity).canRegister();
//        verify(mockActivity).addEnrollment(any(Enrollment.class));
//        verify(mockActivity).incrementPending();
//        verify(mockStudent).addEnrollment(any(Enrollment.class));
//    }
//
//    // ==================== GET MY REQUESTS TESTS ====================
//
////    @Test
////    @DisplayName("Get My Requests - Success returns all enrollments")
////    void getMyRequests_Success_ReturnsAllEnrollments() {
////        // Arrange
////        Enrollment enrollment2 = Enrollment.builder()
////                .enrollmentId(2L)
////                .student(testStudent)
////                .activity(testActivity)
////                .status(EnrollmentStatus.APPROVED)
////                .appliedAt(LocalDateTime.now())
////                .enrollmentDate(LocalDateTime.now())
////                .build();
////
////        List<Enrollment> enrollments = List.of(testEnrollment, enrollment2);
////
////        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
////        when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(enrollments);
////
////        // Act
////        List<EnrollmentResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());
////
////        // Assert
////        assertThat(result).hasSize(2);
////        assertThat(result.get(0).getEnrollmentId()).isEqualTo(testEnrollment.getEnrollmentId());
////        assertThat(result.get(0).getStatus()).isEqualTo(EnrollmentStatus.PENDING);
////        assertThat(result.get(1).getEnrollmentId()).isEqualTo(enrollment2.getEnrollmentId());
////        assertThat(result.get(1).getStatus()).isEqualTo(EnrollmentStatus.APPROVED);
////
////        verify(studentRepository).existsById(testStudent.getStudentId());
////        verify(enrollmentRepository).findByStudentId(testStudent.getStudentId());
////    }
//
//    @Test
//    @DisplayName("Get My Requests - Fails when student not found")
//    void getMyRequests_ThrowsException_WhenStudentNotFound() {
//        // Arrange
//        when(studentRepository.existsById(anyLong())).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.getMyRequests(999L))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
//
//        verify(studentRepository).existsById(999L);
//        verifyNoInteractions(enrollmentRepository);
//    }
//
//    @Test
//    @DisplayName("Get My Requests - Returns empty list when no enrollments")
//    void getMyRequests_ReturnsEmptyList_WhenNoEnrollments() {
//        // Arrange
//        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
//        when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(List.of());
//
//        // Act
//        List<EnrollmentResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());
//
//        // Assert
//        assertThat(result).isEmpty();
//        verify(studentRepository).existsById(testStudent.getStudentId());
//        verify(enrollmentRepository).findByStudentId(testStudent.getStudentId());
//    }
//
//    @Test
//    @DisplayName("Get My Requests - Returns all statuses (PENDING, APPROVED, REJECTED)")
//    void getMyRequests_ReturnsAllStatuses() {
//        // Arrange
//        Enrollment pendingEnrollment = Enrollment.builder()
//                .enrollmentId(1L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.PENDING)
//                .appliedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .build();
//
//        Enrollment approvedEnrollment = Enrollment.builder()
//                .enrollmentId(2L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.APPROVED)
//                .appliedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .build();
//
//        Enrollment rejectedEnrollment = Enrollment.builder()
//                .enrollmentId(3L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.REJECTED)
//                .appliedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .build();
//
//        List<Enrollment> enrollments = List.of(pendingEnrollment, approvedEnrollment, rejectedEnrollment);
//
//        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
//        when(enrollmentRepository.findByStudentId(testStudent.getStudentId())).thenReturn(enrollments);
//
//        // Act
//        List<EnrollmentResponseDto> result = enrollmentService.getMyRequests(testStudent.getStudentId());
//
//        // Assert
//        assertThat(result).hasSize(3);
//        assertThat(result).extracting(EnrollmentResponseDto::getStatus)
//                .containsExactly(EnrollmentStatus.PENDING, EnrollmentStatus.APPROVED, EnrollmentStatus.REJECTED);
//    }
//
//    // ==================== GET MY ACTIVITIES TESTS ====================
//
//    @Test
//    @DisplayName("Get My Activities - Success returns only APPROVED enrollments")
//    void getMyActivities_Success_ReturnsOnlyApprovedEnrollments() {
//        // Arrange
//        Enrollment approvedEnrollment1 = Enrollment.builder()
//                .enrollmentId(1L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.APPROVED)
//                .appliedAt(LocalDateTime.now())
//                .approvedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .isCompleted(false)
//                .build();
//
//        Enrollment approvedEnrollment2 = Enrollment.builder()
//                .enrollmentId(2L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.APPROVED)
//                .appliedAt(LocalDateTime.now())
//                .approvedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .isCompleted(false)
//                .build();
//
//        List<Enrollment> approvedEnrollments = List.of(approvedEnrollment1, approvedEnrollment2);
//
//        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
//        when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
//                .thenReturn(approvedEnrollments);
//
//        // Act
//        List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());
//
//        // Assert
//        assertThat(result).hasSize(2);
//        assertThat(result).allMatch(dto -> dto.getEnrollmentStatus() == EnrollmentStatus.APPROVED);
//        assertThat(result.get(0).getActivityId()).isEqualTo(testActivity.getActivityId());
//        assertThat(result.get(0).getActivityTitle()).isEqualTo(testActivity.getTitle());
//
//        verify(studentRepository).existsById(testStudent.getStudentId());
//        verify(enrollmentRepository).findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED);
//    }
//
//    @Test
//    @DisplayName("Get My Activities - Fails when student not found")
//    void getMyActivities_ThrowsException_WhenStudentNotFound() {
//        // Arrange
//        when(studentRepository.existsById(anyLong())).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.getMyActivities(999L))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
//
//        verify(studentRepository).existsById(999L);
//        verifyNoInteractions(enrollmentRepository);
//    }
//
//    @Test
//    @DisplayName("Get My Activities - Returns empty list when no approved enrollments")
//    void getMyActivities_ReturnsEmptyList_WhenNoApprovedEnrollments() {
//        // Arrange
//        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
//        when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
//                .thenReturn(List.of());
//
//        // Act
//        List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());
//
//        // Assert
//        assertThat(result).isEmpty();
//        verify(studentRepository).existsById(testStudent.getStudentId());
//        verify(enrollmentRepository).findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED);
//    }
//
//    @Test
//    @DisplayName("Get My Activities - Maps activity details correctly")
//    void getMyActivities_MapsActivityDetailsCorrectly() {
//        // Arrange
//        Enrollment approvedEnrollment = Enrollment.builder()
//                .enrollmentId(1L)
//                .student(testStudent)
//                .activity(testActivity)
//                .status(EnrollmentStatus.APPROVED)
//                .appliedAt(LocalDateTime.now())
//                .approvedAt(LocalDateTime.now())
//                .enrollmentDate(LocalDateTime.now())
//                .isCompleted(false)
//                .build();
//
//        when(studentRepository.existsById(testStudent.getStudentId())).thenReturn(true);
//        when(enrollmentRepository.findByStudentIdAndStatus(testStudent.getStudentId(), EnrollmentStatus.APPROVED))
//                .thenReturn(List.of(approvedEnrollment));
//
//        // Act
//        List<MyActivityResponseDto> result = enrollmentService.getMyActivities(testStudent.getStudentId());
//
//        // Assert
//        assertThat(result).hasSize(1);
//        MyActivityResponseDto dto = result.get(0);
//        assertThat(dto.getActivityTitle()).isEqualTo(testActivity.getTitle());
//        assertThat(dto.getShortDescription()).isEqualTo(testActivity.getShortDescription());
//        assertThat(dto.getOrganizationName()).isEqualTo(testOrganization.getOrganizationName());
//        assertThat(dto.getBenefitsCtxh()).isEqualTo(testActivity.getTheNumberOfCtxhDay());
//    }
//
//    // ==================== CANCEL ENROLLMENT TESTS ====================
//
//    @Test
//    @DisplayName("Cancel Enrollment - Success cancels PENDING enrollment")
//    void cancelEnrollment_Success_CancelsPendingEnrollment() {
//        // Arrange
//        Enrollment mockEnrollment = mock(Enrollment.class);
//        Activity mockActivity = mock(Activity.class);
//        Student mockStudent = mock(Student.class);
//
//        when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));
//        when(mockEnrollment.canBeCancelled()).thenReturn(true);
//        when(mockEnrollment.getActivity()).thenReturn(mockActivity);
//        when(mockEnrollment.getStudent()).thenReturn(mockStudent);
//        doNothing().when(enrollmentRepository).delete(mockEnrollment);
//
//        // Act
//        enrollmentService.cancelEnrollment(1L, 1L);
//
//        // Assert
//        verify(enrollmentRepository).findByIdAndStudentId(1L, 1L);
//        verify(mockEnrollment).canBeCancelled();
//        verify(mockActivity).decrementPending();
//        verify(mockStudent).removeEnrollment(mockEnrollment);
//        verify(mockActivity).removeEnrollment(mockEnrollment);
//        verify(enrollmentRepository).delete(mockEnrollment);
//    }
//
//    @Test
//    @DisplayName("Cancel Enrollment - Fails when enrollment not found")
//    void cancelEnrollment_ThrowsException_WhenEnrollmentNotFound() {
//        // Arrange
//        when(enrollmentRepository.findByIdAndStudentId(anyLong(), anyLong())).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(testStudent.getStudentId(), 999L))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_FOUND);
//
//        verify(enrollmentRepository).findByIdAndStudentId(999L, testStudent.getStudentId());
//        verify(enrollmentRepository, never()).delete(any());
//    }
//
//    @Test
//    @DisplayName("Cancel Enrollment - Fails when enrollment belongs to different student")
//    void cancelEnrollment_ThrowsException_WhenNotOwnedByStudent() {
//        // Arrange
//        Long differentStudentId = 999L;
//        when(enrollmentRepository.findByIdAndStudentId(testEnrollment.getEnrollmentId(), differentStudentId))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(differentStudentId, testEnrollment.getEnrollmentId()))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_NOT_FOUND);
//
//        verify(enrollmentRepository).findByIdAndStudentId(testEnrollment.getEnrollmentId(), differentStudentId);
//        verify(enrollmentRepository, never()).delete(any());
//    }
//
//    @Test
//    @DisplayName("Cancel Enrollment - Fails when enrollment cannot be cancelled")
//    void cancelEnrollment_ThrowsException_WhenCannotBeCancelled() {
//        // Arrange
//        Enrollment mockEnrollment = mock(Enrollment.class);
//
//        when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));
//        when(mockEnrollment.canBeCancelled()).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L, 1L))
//                .isInstanceOf(BusinessException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
//
//        verify(enrollmentRepository).findByIdAndStudentId(1L, 1L);
//        verify(mockEnrollment).canBeCancelled();
//        verify(enrollmentRepository, never()).delete(any());
//    }
//
//    @Test
//    @DisplayName("Cancel Enrollment - Calls helper methods in correct order")
//    void cancelEnrollment_CallsHelperMethodsCorrectly() {
//        // Arrange
//        Enrollment mockEnrollment = mock(Enrollment.class);
//        Activity mockActivity = mock(Activity.class);
//        Student mockStudent = mock(Student.class);
//
//        when(enrollmentRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(mockEnrollment));
//        when(mockEnrollment.canBeCancelled()).thenReturn(true);
//        when(mockEnrollment.getActivity()).thenReturn(mockActivity);
//        when(mockEnrollment.getStudent()).thenReturn(mockStudent);
//
//        // Act
//        enrollmentService.cancelEnrollment(1L, 1L);
//
//        // Assert - Verify order of operations
//        verify(mockActivity).decrementPending();
//        verify(mockStudent).removeEnrollment(mockEnrollment);
//        verify(mockActivity).removeEnrollment(mockEnrollment);
//        verify(enrollmentRepository).delete(mockEnrollment);
//    }
//}
