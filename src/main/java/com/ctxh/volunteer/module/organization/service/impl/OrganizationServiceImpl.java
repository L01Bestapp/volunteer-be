package com.ctxh.volunteer.module.organization.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.attendance.repository.AttendanceRepository;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.service.AuthService;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.ActivityStatsDto;
import com.ctxh.volunteer.module.organization.dto.response.ImpactStatsDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationStatisticsResponseDto;
import com.ctxh.volunteer.module.organization.dto.response.ParticipantStatsDto;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import com.ctxh.volunteer.module.organization.repository.OrganizationRepository;
import com.ctxh.volunteer.module.organization.service.OrganizationService;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ctxh.volunteer.common.util.AppConstants.DEFAULT_AVATAR_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public OrganizationResponseDto registerOrganization(CreateOrganizationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        // Validate organization uniqueness
        if (organizationRepository.existsByOrganizationName(requestDto.getOrganizationName())) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NAME_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .avatarUrl(DEFAULT_AVATAR_URL)
                .roles(List.of(role))
                .build();

        // Create an organization entity
        Organization organization = Organization.builder()
                .user(user)
                .organizationName(requestDto.getOrganizationName())
                .type(OrganizationType.valueOf(requestDto.getOrganizationType()))
                .build();


        user.setOrganization(organization);
        userRepository.save(user);
        Organization savedOrganization = user.getOrganization();
        log.info("Created organization with ID: {}", savedOrganization.getOrganizationId());

        authService.resendVerificationEmail(requestDto.getEmail());

        return mapToOrganizationResponseDto(savedOrganization);
    }

    @Override
    public OrganizationResponseDto updateOrganization(Long organizationId, UpdateOrganizationRequestDto requestDto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));

        User user = organization.getUser();

        if (requestDto.getBio() != null) {
            user.setBio(requestDto.getBio());
        }

        if (requestDto.getOrganizationType() != null) {
            organization.setType(OrganizationType.valueOf(requestDto.getOrganizationType()));
        }

        if (requestDto.getRepresentativeEmail() != null) {
            organization.setRepresentativeEmail(requestDto.getRepresentativeEmail());
        }

        if (requestDto.getRepresentativeName() != null) {
            organization.setRepresentativeName(requestDto.getRepresentativeName());
        }

        if (requestDto.getRepresentativePhoneNumber() != null) {
            organization.setRepresentativePhone(requestDto.getRepresentativePhoneNumber());
        }
        log.info("Updated organization with ID: {}", organization.getOrganizationId());

        userRepository.save(user);
        return mapToOrganizationResponseDto(organization);
    }

    @Override
    public OrganizationResponseDto getOrganizationById(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));
        return mapToOrganizationResponseDto(organization);
    }

    @Override
    public void activeOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));
        organization.activeOrganization();
        organizationRepository.save(organization);
    }

    @Override
    public List<OrganizationResponseDto> getAllOrganization() {
        return organizationRepository.findAll().stream().map(this::mapToOrganizationResponseDto).toList();
    }

    @Override
    public OrganizationStatisticsResponseDto getStatistics(Long organizationId) {
        // Verify organization exists
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // 1. Activity Stats
        Long totalActivities = activityRepository.countByOrganizationId(organizationId);
        Long upcomingCount = activityRepository.countByOrganizationIdAndStatus(organizationId, ActivityStatus.UPCOMING);
        Long ongoingCount = activityRepository.countByOrganizationIdAndStatus(organizationId, ActivityStatus.ONGOING);
        Long completedCount = activityRepository.countByOrganizationIdAndStatus(organizationId, ActivityStatus.COMPLETED);
        Long canceledCount = 0L; // Not implemented yet - can be added later if needed

        ActivityStatsDto activityStats = ActivityStatsDto.builder()
                .totalActivities(totalActivities)
                .upcomingCount(upcomingCount)
                .ongoingCount(ongoingCount)
                .completedCount(completedCount)
                .canceledCount(canceledCount)
                .build();

        // 2. Participant Stats
        Long totalSlots = activityRepository.sumMaxParticipantsByOrganizationId(organizationId);
        Long totalRegistrations = enrollmentRepository.countByOrganizationId(organizationId);
        Long totalApproved = enrollmentRepository.countApprovedByOrganizationId(organizationId);
        Long totalAttended = attendanceRepository.countPresentByOrganizationId(organizationId);

        // Calculate attendance rate: (Attended / Approved) * 100
        Double avgAttendanceRate = 0.0;
        if (totalApproved > 0) {
            avgAttendanceRate = (totalAttended.doubleValue() / totalApproved.doubleValue()) * 100.0;
            avgAttendanceRate = Math.round(avgAttendanceRate * 100.0) / 100.0; // Round to 2 decimal places
        }

        ParticipantStatsDto participantStats = ParticipantStatsDto.builder()
                .totalSlots(totalSlots)
                .totalRegistrations(totalRegistrations)
                .totalApproved(totalApproved)
                .totalAttended(totalAttended)
                .avgAttendanceRate(avgAttendanceRate)
                .build();

        // 3. Impact Stats
        Double totalCtxhDaysGenerated = attendanceRepository.sumCtxhDaysByOrganizationId(organizationId);
        totalCtxhDaysGenerated = Math.round(totalCtxhDaysGenerated * 10.0) / 10.0; // Round to 1 decimal place

        ImpactStatsDto impactStats = ImpactStatsDto.builder()
                .totalCtxhDaysGenerated(totalCtxhDaysGenerated)
                .build();

        log.info("Retrieved statistics for organization ID: {}", organizationId);

        return OrganizationStatisticsResponseDto.builder()
                .activityStats(activityStats)
                .participantStats(participantStats)
                .impactStats(impactStats)
                .build();
    }

    private OrganizationResponseDto mapToOrganizationResponseDto(Organization organization) {
        return OrganizationResponseDto.builder()
                .organizationId(organization.getOrganizationId())
                .organizationName(organization.getOrganizationName())
                .type(organization.getType())
                .representativeEmail(organization.getRepresentativeEmail())
                .representativePhoneNumber(organization.getRepresentativePhone())
                .representativeName(organization.getRepresentativeName())
                .email(organization.getUser().getEmail())
                .avatarUrl(organization.getUser().getAvatarUrl())
                .bio(organization.getUser().getBio())
                .createdAt(organization.getCreateAt())
                .build();
    }
}
