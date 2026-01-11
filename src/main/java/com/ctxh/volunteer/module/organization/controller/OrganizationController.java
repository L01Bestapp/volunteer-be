package com.ctxh.volunteer.module.organization.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.common.util.AuthUtil;
import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import com.ctxh.volunteer.module.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    /**
     * Create new organization
     * POST /api/v1/organizations/register
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements()
    public ApiResponse<OrganizationResponseDto> createOrganization(
            @Valid @RequestBody CreateOrganizationRequestDto requestDto) {
        return ApiResponse.ok(
                "Organization created successfully",
                organizationService.registerOrganization(requestDto)
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrganizationResponseDto>> getAllOrganizations() {
        return ApiResponse.ok(organizationService.getAllOrganization());
    }

    @PutMapping("/{id}/active")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activeOrganization(@PathVariable("id") Long organizationId) {
        organizationService.activeOrganization(organizationId);
        return ApiResponse.ok("active account successfully");
    }

//    sau này sửa lại cái id lấy từ Authenticated user
    /**
     * Update organization information
     * PUT /api/v1/organizations/{id}
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ApiResponse<OrganizationResponseDto> updateOrganization(
            @Valid @RequestBody UpdateOrganizationRequestDto requestDto) {
        Long organizationId = AuthUtil.getIdFromAuthentication();
        return ApiResponse.ok(
                "updated Organization successfully",
                organizationService.updateOrganization(organizationId, requestDto)
        );
    }

    /**
    * Get organization by ID
    * GET /api/v1/organization/{id}
    */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ApiResponse<OrganizationResponseDto> getOrganizationById() {
        Long organizationId = AuthUtil.getIdFromAuthentication();
        return ApiResponse.ok(
                "retrieved Organization successfully",
                organizationService.getOrganizationById(organizationId)
        );
    }
}
