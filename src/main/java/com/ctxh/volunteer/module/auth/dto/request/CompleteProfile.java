package com.ctxh.volunteer.module.auth.dto.request;

import com.ctxh.volunteer.common.util.EnumValidation;
import com.ctxh.volunteer.module.student.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteProfile {
    @NotBlank(message = "User ID is required")
    private Long userId;

    @Schema(description = "User's password", example = "thangvip030201")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "MSSV is required")
    @Pattern(regexp = "^\\d{7}$", message = "MSSV must be exactly 7 digits")
    private String mssv;

    @EnumValidation(name = "Gender", enumClass = Gender.class)
    private String gender;
}
