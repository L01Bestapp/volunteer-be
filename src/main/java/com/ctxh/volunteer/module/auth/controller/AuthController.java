package com.ctxh.volunteer.module.auth.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.auth.config.Oauth2UrlBuilder;
import com.ctxh.volunteer.module.auth.dto.request.CompleteProfile;
import com.ctxh.volunteer.module.auth.dto.request.LoginRequest;
import com.ctxh.volunteer.module.auth.dto.request.ResetPasswordRequest;
import com.ctxh.volunteer.module.auth.dto.response.GoogleSignInResponseDto;
import com.ctxh.volunteer.module.auth.dto.response.TokenResponse;
import com.ctxh.volunteer.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final Oauth2UrlBuilder oauth2UrlBuilder;

    @Operation(description = "Login", summary = "Authenticate user and return a access token and refresh token")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.ok("login successful", authService.login(request));
    }

    @PostMapping("/complete-profile")
    public ApiResponse<Void> completeProfile(@RequestBody @Valid CompleteProfile request) {
        authService.completeStudentProfile(request);
        return ApiResponse.ok("complete profile successful", null);
    }

    @GetMapping("social-login")
    @Operation(summary = "Social login", description = "Login using social media account")
    public ApiResponse<String> socialLogin(@RequestParam("loginType") String loginType, HttpServletRequest request){
        return ApiResponse.ok("Social login successful"
                ,oauth2UrlBuilder.buildAuthorizationUrl(request, loginType.toLowerCase()));
    }

    @PostMapping("/social-callback/{registrationId}")
    @Operation(summary = "OAuth2 login", description = "Login using OAuth2 provider")
    public ApiResponse<GoogleSignInResponseDto> oauth2CallBack(@PathVariable("registrationId") String loginType,
                                                               @RequestParam ("code") String code){
        return ApiResponse.ok("OAth2 login successful"
                ,authService.oauth2CallBack(loginType.toLowerCase(),code));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke all tokens for the user")
    public ApiResponse<Void> logout(@RequestHeader("re-token") String refreshToken) {
        authService.logout(refreshToken);
        return ApiResponse.ok("Logout successful",null);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Refresh access token using refresh token")
    public ApiResponse<TokenResponse> refreshToken(@RequestHeader("re-token") String refreshToken) {

        return ApiResponse.ok("Access token refreshed successfully", authService.refreshToken(refreshToken));
    }

    @Operation(description = "Verify email", summary = "Verify email and enable account")
    @GetMapping("/verify-email")
    public void verifyEmailAndEnableAccount(@RequestParam("token") String token,
                                            HttpServletResponse response) throws IOException {
        authService.verifyEmailAndEnableAccount(token);
//        response.sendRedirect("https://www.facebook.com");
    }

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend-verify-email")
    public ApiResponse<Void> resendVerifyEmail(@RequestParam("email") String email) {
        authService.resendVerificationEmail(email);
        return ApiResponse.ok("Verification email sent again", null);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send reset password email")
    public ApiResponse<Void> forgotPassword(@RequestParam("email") String email) {
        authService.forgotPassword(email);
        return ApiResponse.ok("If the email exists, you will receive a email recovery guide.", null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successfully", null);
    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Upload image for the user")
    @PostAuthorize("returnObject.data == authentication.name")
    public ApiResponse<String> uploadImage(@RequestPart("avatar") MultipartFile avatar) {
        return ApiResponse.ok("Image uploaded successfully", authService.uploadImage(avatar));
    }
}
