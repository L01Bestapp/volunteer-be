package com.ctxh.volunteer.module.organization.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
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

import static com.ctxh.volunteer.common.util.AppConstants.DEFAULT_AVATAR_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public OrganizationResponseDto registerOrganization(CreateOrganizationRequestDto requestDto) {
        // Validate organization uniqueness
        if (organizationRepository.existsByOrganizationName(requestDto.getOrganizationName())) {
            throw new BusinessException(ErrorCode.ORGANIZATION_NAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .avatarUrl(DEFAULT_AVATAR_URL)
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
