package com.ctxh.volunteer.module.auth.dto.request;

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
public class LoginRequest {
    @Schema(description = "User's email address", example = "thangvip030201@gmail.com")
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@hcmut\\.edu\\.vn$", message = "email must be a valid HCMUT email address")
    private String email;

    @Schema(description = "User's password", example = "thangvip030201")
    @NotBlank(message = "Password is required")
    private String password;
}
