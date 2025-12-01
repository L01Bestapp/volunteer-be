package com.ctxh.volunteer.module.auth.config;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityErrorResponseUtil securityErrorResponseUtil;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        BusinessException appEx = extractAppException(authException);
        if (appEx != null) {
            securityErrorResponseUtil.writeErrorResponse(response, appEx.getErrorCode());
        }
        else
            securityErrorResponseUtil.writeErrorResponse(response, ErrorCode.UNAUTHENTICATED);
    }

    private BusinessException extractAppException(Throwable ex) {
        while (ex != null) {
            if (ex instanceof BusinessException appException) {
                return appException;
            }
            ex = ex.getCause();
        }
        return null;
    }
}
