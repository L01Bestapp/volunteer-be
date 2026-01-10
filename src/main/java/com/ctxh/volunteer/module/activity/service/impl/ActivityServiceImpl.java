package com.ctxh.volunteer.module.activity.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.common.util.AuthUtil;
import com.ctxh.volunteer.common.util.ImageValidator;
import com.ctxh.volunteer.module.activity.dto.request.CreateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.request.UpdateActivityRequestDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityListResponseDto;
import com.ctxh.volunteer.module.activity.dto.response.ActivityResponseDto;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.activity.service.ActivityService;
import com.ctxh.volunteer.module.activity.specification.ActivitySpecification;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.dto.EnrollmentResponseDto;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.repository.OrganizationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final OrganizationRepository organizationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ImageValidator imageValidator;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public ActivityResponseDto createActivity(CreateActivityRequestDto requestDto, MultipartFile imageFile) {
        Long organizationId = AuthUtil.getIdFromAuthentication();

        // Find organization
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));

        if(!organization.canCreateActivities()) throw new BusinessException(ErrorCode.ORGANIZATION_NOT_VERIFIED);

        // Validate dates
        if (requestDto.getEndDateTime().isBefore(requestDto.getStartDateTime())) {
            throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DATE);
        }

        LocalDateTime registrationOpensAt = LocalDateTime.now();

        if (requestDto.getRegistrationDeadline().isBefore(registrationOpensAt)) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_DEADLINE);
        }

        String imageUrl = uploadImage(imageFile);

        // Create activity
        Activity activity = Activity.builder()
                .organization(organization)
                .title(requestDto.getTitle())
                .imageUrl(imageUrl)
                .description(requestDto.getDescription())
                .shortDescription(requestDto.getShortDescription())
                .category(requestDto.getCategory() != null ? ActivityCategory.valueOf(requestDto.getCategory()) : null)
                .startDateTime(requestDto.getStartDateTime())
                .endDateTime(requestDto.getEndDateTime())
                .registrationOpensAt(registrationOpensAt)
                .registrationDeadline(requestDto.getRegistrationDeadline())
                .address(requestDto.getAddress())
                .maxParticipants(requestDto.getMaxParticipants())
                .requirements(requestDto.getRequirements())
                .theNumberOfCtxhDay(requestDto.getTheNumberOfCtxhDay())
                .registrationState(RegistrationState.OPEN)
                .activityStatus(ActivityStatus.UPCOMING)
                .build();

        // Set organization
        organization.addActivity(activity);
        Activity savedActivity = activityRepository.save(activity);
        log.info("Created activity with ID: {} for organization: {}", savedActivity.getActivityId(), organizationId);

        return mapToActivityResponseDto(savedActivity);
    }

    private String uploadImage(MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        imageValidator.validate(avatar);
        try {
            Map<?,?> uploadResult = cloudinary.uploader().upload(avatar.getBytes(), ObjectUtils.emptyMap());
            log.info("Uploaded image to Cloudinary: {}", uploadResult);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityListResponseDto> getActivitiesByOrganization(Long organizationId) {
        // Verify organization exists
        if (!organizationRepository.existsById(organizationId)) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        List<Activity> activities = activityRepository.findByOrganizationId(organizationId);
        return activities.stream()
                .map(this::mapToActivityListResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDto getActivityById(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        return mapToActivityResponseDto(activity);
    }

    @Override
    @Transactional
    public ActivityResponseDto updateActivity(Long organizationId, Long activityId, @Valid UpdateActivityRequestDto requestDto) {
        // Find activity and verify ownership
        Activity activity = activityRepository.findByIdAndOrganizationId(activityId, organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Check if activity can be modified
        if (activity.getRegistrationState() == RegistrationState.COMPLETED) {
            throw new BusinessException(ErrorCode.ACTIVITY_ALREADY_COMPLETED);
        }

        // Update fields if provided
        if (requestDto.getName() != null) {
            activity.setTitle(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            activity.setDescription(requestDto.getDescription());
        }
        if (requestDto.getShortDescription() != null) {
            activity.setShortDescription(requestDto.getShortDescription());
        }
        if (requestDto.getCategory() != null) {
            activity.setCategory(ActivityCategory.valueOf(requestDto.getCategory()));
        }
        if (requestDto.getStartDateTime() != null) {
            activity.setStartDateTime(requestDto.getStartDateTime());
        }
        if (requestDto.getEndDateTime() != null) {
            activity.setEndDateTime(requestDto.getEndDateTime());
        }
        if (requestDto.getRegistrationDeadline() != null) {
            activity.setRegistrationDeadline(requestDto.getRegistrationDeadline());
        }
        if (requestDto.getAddress() != null) {
            activity.setAddress(requestDto.getAddress());
        }
        if (requestDto.getMaxParticipants() != null) {
            activity.setMaxParticipants(requestDto.getMaxParticipants());
        }
        if (requestDto.getRequirements() != null) {
            activity.setRequirements(requestDto.getRequirements());
        }
        if (requestDto.getTheNumberOfCtxhDay() != null) {
            activity.setTheNumberOfCtxhDay(requestDto.getTheNumberOfCtxhDay());
        }

        // Validate dates if both are present
        if (activity.getEndDateTime().isBefore(activity.getStartDateTime())) {
            throw new BusinessException(ErrorCode.INVALID_ACTIVITY_DATE);
        }

        Activity updatedActivity = activityRepository.save(activity);
        log.info("Updated activity with ID: {}", activityId);

        return mapToActivityResponseDto(updatedActivity);
    }

    @Override
    @Transactional
    public void deleteActivity(Long organizationId, Long activityId) {
        // Find activity and verify ownership
        Activity activity = activityRepository.findByIdAndOrganizationId(activityId, organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Check if activity can be deleted
        if (activity.getRegistrationState() == RegistrationState.COMPLETED) {
            throw new BusinessException(ErrorCode.ACTIVITY_ALREADY_COMPLETED);
        }

        if (activity.hasStarted()) {
            throw new BusinessException(ErrorCode.ACTIVITY_ALREADY_STARTED);
        }

        activityRepository.delete(activity);
        log.info("Deleted activity with ID: {}", activityId);
    }

    @Override
    @Transactional
    public ActivityResponseDto closeActivityRegistration(Long organizationId, Long activityId) {
        // Find activity and verify ownership
        Activity activity = activityRepository.findByIdAndOrganizationId(activityId, organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Check if activity is open
        if (activity.getRegistrationState() != RegistrationState.OPEN && activity.getRegistrationState() != RegistrationState.FULL) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_OPEN);
        }

        // Close registration
        activity.closeRegistration();
        Activity closedActivity = activityRepository.save(activity);
        log.info("Closed registration for activity with ID: {}", activityId);

        return mapToActivityResponseDto(closedActivity);
    }

    // ============ ENROLLMENT MANAGEMENT ============

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getActivityEnrollments(Long activityId) {
        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByActivityId(activityId);
        return enrollments.stream()
                .map(this::mapToEnrollmentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getPendingEnrollments(Long activityId) {
        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByActivityIdAndStatus(
                activityId,
                EnrollmentStatus.PENDING
        );
        return enrollments.stream()
                .map(this::mapToEnrollmentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getApprovedEnrollments(Long activityId) {
        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByActivityIdAndStatus(
                activityId,
                EnrollmentStatus.APPROVED
        );
        return enrollments.stream()
                .map(this::mapToEnrollmentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getRejectedEnrollments(Long activityId) {
        // Verify activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByActivityIdAndStatus(
                activityId,
                EnrollmentStatus.REJECTED
        );
        return enrollments.stream()
                .map(this::mapToEnrollmentResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public EnrollmentResponseDto approveEnrollment(Long activityId, Long enrollmentId, Long approvedByUserId) {
        // Find enrollment and verify it belongs to the activity
        Enrollment enrollment = enrollmentRepository.findByIdAndActivityId(enrollmentId, activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Check if enrollment is pending
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        // Approve enrollment
        enrollment.approve(approvedByUserId);
        Enrollment approvedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Approved enrollment ID: {} for activity ID: {} by user ID: {}",
                enrollmentId, activityId, approvedByUserId);

        return mapToEnrollmentResponseDto(approvedEnrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponseDto rejectEnrollment(Long activityId, Long enrollmentId, Long rejectedByUserId) {
        // Find enrollment and verify it belongs to the activity
        Enrollment enrollment = enrollmentRepository.findByIdAndActivityId(enrollmentId, activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // Check if enrollment is pending
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        // Reject enrollment
        enrollment.reject(rejectedByUserId);
        Enrollment rejectedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Rejected enrollment ID: {} for activity ID: {} by user ID: {}",
                enrollmentId, activityId, rejectedByUserId);

        return mapToEnrollmentResponseDto(rejectedEnrollment);
    }

    // ============ STUDENT DISCOVERY APIs ============

    @Override
    @Transactional(readOnly = true)
    public List<ActivityListResponseDto> getAllActivity() {
        List<Activity> activities = activityRepository.findAllActivityOrderByDESC();
        return activities.stream()
                .map(this::mapToActivityListResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityListResponseDto> searchActivities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActivity();
        }

        List<Activity> activities = activityRepository.searchByKeyword(keyword.trim());
        return activities.stream()
                .map(this::mapToActivityListResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponseDto getActivityDetail(Long activityId) {
        // Same as getActivityById but for students
        return getActivityById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityListResponseDto> searchActivitiesAdvanced(
            String keyword,
            ActivityCategory category,
            RegistrationState status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // Build specification and execute query
        Specification<Activity> spec = ActivitySpecification.searchActivities(
                keyword,
                category,
                status,
                startDate,
                endDate
        );

        List<Activity> activities = activityRepository.findAll(spec);
        return activities.stream()
                .map(this::mapToActivityListResponseDto)
                .toList();
    }

    // ============ MAPPING METHODS ============

    private ActivityResponseDto mapToActivityResponseDto(Activity activity) {
        return ActivityResponseDto.builder()
                .activityId(activity.getActivityId())
                .organizationId(activity.getOrganization().getOrganizationId())
                .organizationName(activity.getOrganization().getOrganizationName())
                .name(activity.getTitle())
                .imageUrl(activity.getImageUrl())
                .description(activity.getDescription())
                .shortDescription(activity.getShortDescription())
                .category(activity.getCategory())
                .startDateTime(activity.getStartDateTime())
                .endDateTime(activity.getEndDateTime())
                .registrationOpensAt(activity.getRegistrationOpensAt())
                .registrationDeadline(activity.getRegistrationDeadline())
                .address(activity.getAddress())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(activity.getCurrentParticipants())
                .pendingParticipants(activity.getPendingParticipants())
                .approvedParticipants(activity.getApprovedParticipants())
                .remainingSlots(activity.getRemainingSlots())
                .registrationState(activity.getRegistrationState())
                .activityStatus(activity.getActivityStatus())
                .requirements(activity.getRequirements())
                .benefitsCtxh(activity.getTheNumberOfCtxhDay())
                .createdAt(activity.getCreateAt())
                .updatedAt(activity.getUpdateAt())
                .build();
    }

    private ActivityListResponseDto mapToActivityListResponseDto(Activity activity) {
        return ActivityListResponseDto.builder()
                .activityId(activity.getActivityId())
                .title(activity.getTitle())
                .shortDescription(activity.getShortDescription())
                .imageUrl(activity.getImageUrl())
                .category(activity.getCategory())
                .registrationDeadline(activity.getRegistrationDeadline())
                .theNumberOfCtxhDay(activity.getTheNumberOfCtxhDay())
                .startDateTime(activity.getStartDateTime())
                .endDateTime(activity.getEndDateTime())
                .address(activity.getAddress())
                .maxParticipants(activity.getMaxParticipants())
                .approvedParticipants(activity.getApprovedParticipants())
                .remainingSlots(activity.getRemainingSlots())
                .registrationState(activity.getRegistrationState())
                .activityStatus(activity.getActivityStatus())
                .createdAt(activity.getCreateAt())
                .build();
    }

    private EnrollmentResponseDto mapToEnrollmentResponseDto(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .status(enrollment.getStatus())
                .appliedAt(enrollment.getAppliedAt())
                .approvedAt(enrollment.getApprovedAt())
                .approvedBy(enrollment.getApprovedBy())
                .rejectedAt(enrollment.getRejectedAt())
                .rejectedBy(enrollment.getRejectedBy())
                .isCompleted(enrollment.getIsCompleted())
                .completedAt(enrollment.getCompletedAt())
                .studentId(enrollment.getStudent().getStudentId())
                .fullName(enrollment.getStudent().getFullName())
                .mssv(enrollment.getStudent().getMssv())
                .email(enrollment.getStudent().getUser().getEmail())
                .phoneNumber(enrollment.getStudent().getPhoneNumber())
                .academicYear(enrollment.getStudent().getAcademicYear())
                .faculty(enrollment.getStudent().getFaculty())
                .gender(enrollment.getStudent().getGender())
                .dateOfBirth(enrollment.getStudent().getDateOfBirth())
                .totalCtxhDays(enrollment.getStudent().getTotalCtxhDays())
                .build();
    }
}
