package com.ctxh.volunteer.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @Schema(description = "User's email address", example = "thang.vokhmt04k22@hcmut.edu.vn")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "User's password", example = "thangvip123")
    @NotBlank(message = "Password is required")
    private String password;
}
