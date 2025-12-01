package com.ctxh.volunteer.module.auth.config;


import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SecurityErrorResponseUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<?> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
