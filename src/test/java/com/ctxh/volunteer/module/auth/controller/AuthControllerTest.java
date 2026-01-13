package com.ctxh.volunteer.module.auth.controller;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.auth.dto.request.*;
import com.ctxh.volunteer.module.auth.dto.response.GoogleSignInResponseDto;
import com.ctxh.volunteer.module.auth.dto.response.TokenResponse;
import com.ctxh.volunteer.module.auth.dto.response.VerifyOtpResponse;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {AuthController.class, com.ctxh.volunteer.common.exception.GlobalExceptionHandler.class},
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = com.ctxh.volunteer.module.auth.config.CustomAuthenticationConverter.class
    ),
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    })
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    private LoginRequest loginRequest;
    private TokenResponse tokenResponse;
    private GoogleIdTokenRequest googleIdTokenRequest;
    private GoogleSignInResponseDto googleSignInResponse;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

        // Setup Login Request
        loginRequest = new LoginRequest("test@email.com", "password123");

        // Setup Token Response
        tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .role(RoleEnum.STUDENT.name())
                .build();

        // Setup Google Request
        googleIdTokenRequest = new GoogleIdTokenRequest("google-id-token");
        googleSignInResponse = GoogleSignInResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .role(RoleEnum.STUDENT.name())
                .profileComplete(true)
                .build();
    }

    @Test
    @DisplayName("POST /login - Success returns token")
    void login_Success_ReturnsToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /login - Fails with invalid credentials")
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /ban-user - Success bans user")
    void banUser_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).banUser(1L);

        mockMvc.perform(post("/api/v1/auth/ban-user")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /un-ban-user - Success unlocks user")
    void unBanUser_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).unBanUser(1L);

        mockMvc.perform(post("/api/v1/auth/un-ban-user")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /sign-up-with-google-for-student - Success returns tokens")
    void signUpWithGoogle_Success_ReturnsTokens() throws Exception {
        when(authService.verifyGoogleIdTokenForStudent(anyString())).thenReturn(googleSignInResponse);

        mockMvc.perform(post("/api/v1/auth/sign-up-with-google-for-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /login-with-google - Success returns tokens")
    void loginWithGoogle_Success_ReturnsTokens() throws Exception {
        when(authService.loginWithGoogle(anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login-with-google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleIdTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /complete-profile - Success completes profile")
    void completeProfile_Success_ReturnsOk() throws Exception {
        CompleteProfile request = CompleteProfile.builder()
                .userId(1L)
                .password("password123")
                .mssv("2012345")
                .gender("MALE")
                .build();
        doNothing().when(authService).completeStudentProfile(any(CompleteProfile.class));

        mockMvc.perform(post("/api/v1/auth/complete-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /logout - Success logs out")
    void logout_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("re-token", "refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /refresh-token - Success refreshes token")
    void refreshToken_Success_ReturnsNewToken() throws Exception {
        when(authService.refreshToken(anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("re-token", "refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("GET /verify-email - Success verifies email")
    void verifyEmail_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).verifyEmailAndEnableAccount(anyString());

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", "verification-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /resend-verify-email - Success resends email")
    void resendVerifyEmail_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).resendVerificationEmail(anyString());

        mockMvc.perform(post("/api/v1/auth/resend-verify-email")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /forgot-password - Success sends email")
    void forgotPassword_Success_ReturnsOk() throws Exception {
        doNothing().when(authService).forgotPassword(anyString());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /verify-otp - Success returns token")
    void verifyOtp_Success_ReturnsToken() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("test@email.com", "123456");
        VerifyOtpResponse response = new VerifyOtpResponse("reset-token");
        when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resetPasswordToken").value("reset-token"));
    }

    @Test
    @DisplayName("POST /reset-password - Success resets password")
    void resetPassword_Success_ReturnsOk() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword123", "newPassword123");
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /change-password - Success changes password")
    void changePassword_Success_ReturnsOk() throws Exception {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("oldPassword", "newPassword", "newPassword");
        doNothing().when(authService).changePassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
