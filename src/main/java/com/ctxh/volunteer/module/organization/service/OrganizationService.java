package com.ctxh.volunteer.module.organization.service;

import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationStatisticsResponseDto;

import java.util.List;

public interface OrganizationService {
    OrganizationResponseDto registerOrganization(CreateOrganizationRequestDto requestDto);

    OrganizationResponseDto updateOrganization(Long organizationId, UpdateOrganizationRequestDto requestDto);

    OrganizationResponseDto getOrganizationById(Long organizationId);

    void activeOrganization(Long organizationId);

    List<OrganizationResponseDto> getAllOrganization();

    OrganizationStatisticsResponseDto getStatistics(Long organizationId);
}
