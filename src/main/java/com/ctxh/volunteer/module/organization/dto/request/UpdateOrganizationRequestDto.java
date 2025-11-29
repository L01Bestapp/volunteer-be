package com.ctxh.volunteer.module.organization.dto.request;

import com.ctxh.volunteer.common.util.EnumValidation;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrganizationRequestDto {
    @Pattern(regexp = "^([\\p{L}]{2,})(\\s+[\\p{L}]{2,}){1,6}$",
            message = "Representative Name must contain only letters and spaces, with at least two words")
    @Size(max = 100, message = "Representative Name must not exceed 100 characters")
    private String representativeName;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "email must be a valid email address")
    private String representativeEmail;

    @Pattern(regexp = "^\\+?84\\d{9}$", message = "Phone number must be a valid Vietnam phone number")
    private String representativePhoneNumber;

    private String bio;

    @EnumValidation(name = "OrganizationType", enumClass = OrganizationType.class)
    private String organizationType;
}
