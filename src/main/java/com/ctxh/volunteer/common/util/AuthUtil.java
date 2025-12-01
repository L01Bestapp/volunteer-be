package com.ctxh.volunteer.common.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    private AuthUtil() {}

    public static Long getIdFromAuthentication() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return Long.valueOf(userId);
    }
}
