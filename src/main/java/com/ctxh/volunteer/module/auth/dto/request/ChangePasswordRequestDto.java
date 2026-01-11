package com.ctxh.volunteer.module.auth.dto.request;


import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequestDto(
        @NotBlank(message = "currentPassword not be blank")
        String currentPassword,

        @NotBlank(message = "newPassword not be blank")
        String newPassword,

        @NotBlank(message = "confirmNewPassword not be blank")
        String confirmNewPassword
) {
}
