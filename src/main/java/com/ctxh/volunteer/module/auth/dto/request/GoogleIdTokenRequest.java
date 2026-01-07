package com.ctxh.volunteer.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleIdTokenRequest(@NotBlank String idToken) {
}
