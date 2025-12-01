package com.ctxh.volunteer.module.organization.service;

import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import jakarta.validation.Valid;

public interface OrganizationService {
    OrganizationResponseDto registerOrganization(@Valid CreateOrganizationRequestDto requestDto);

    OrganizationResponseDto updateOrganization(Long organizationId, @Valid UpdateOrganizationRequestDto requestDto);

    OrganizationResponseDto getOrganizationById(Long organizationId);
}
