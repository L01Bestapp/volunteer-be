package com.ctxh.volunteer.module.organization.dto.request;

import com.ctxh.volunteer.common.util.EnumValidation;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrganizationRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Organization name is required")
    @Size(max = 200, message = "Organization name must not exceed 200 characters")
    private String organizationName;

    @EnumValidation(name = "OrganizationType", enumClass = OrganizationType.class)
    @NotBlank(message = "Organization type is required")
    private String organizationType;

    @Pattern(regexp = "^\\+?(84|0)\\d{9}$", message = "Phone number must be a valid Vietnam phone number")
    private String phoneNumber;

}
