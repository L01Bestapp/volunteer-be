package com.ctxh.volunteer.module.auth.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.auth.dto.request.CompleteProfile;
import com.ctxh.volunteer.module.auth.dto.request.GoogleIdTokenRequest;
import com.ctxh.volunteer.module.auth.dto.request.LoginRequest;
import com.ctxh.volunteer.module.auth.dto.request.ResetPasswordRequest;
import com.ctxh.volunteer.module.auth.dto.request.VerifyOtpRequest;
import com.ctxh.volunteer.module.auth.dto.response.GoogleSignInResponseDto;
import com.ctxh.volunteer.module.auth.dto.response.TokenResponse;
import com.ctxh.volunteer.module.auth.dto.response.VerifyOtpResponse;
import com.ctxh.volunteer.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @Operation(description = "Login for student", summary = "Authenticate student and return a access toke, role and refresh token")
    @SecurityRequirements() // No authentication required for login
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.ok("User login success", authService.login(request));
    }

    @Operation(summary = "Ban user", description = "Ban user from using the application")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ban-user")
    public ApiResponse<Void> banUser(@RequestParam("userId") Long userId){
        authService.banUser(userId);
        return ApiResponse.ok("User banned successfully");
    }


    @Operation(summary = "Unlock Ban user", description = "Unlock Ban user from using the application")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/un-ban-user")
    public ApiResponse<Void> unlockBanUser(@RequestParam("userId") Long userId){
        authService.unBanUser(userId);
        return ApiResponse.ok("User unban successfully");
    }



    @Operation(description = "Login with Google", summary = "Authenticate with Google and return JWT tokens")
    @SecurityRequirements()
    @PostMapping("/google")
    public ApiResponse<GoogleSignInResponseDto> loginWithGoogle(@RequestBody @Valid GoogleIdTokenRequest request){
        return ApiResponse.ok("Google sign-in successful", authService.verifyGoogleIdToken(request.idToken()));
    }

    @Operation(summary = "Complete profile", description = "Complete user profile after registration")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/complete-profile")
    public ApiResponse<Void> completeProfile(@RequestBody @Valid CompleteProfile request) {
        authService.completeStudentProfile(request);
        return ApiResponse.ok("complete profile successful", null);
    }

    @Operation(summary = "Logout", description = "Revoke all tokens for the user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("re-token") String refreshToken) {
        authService.logout(refreshToken);
        return ApiResponse.ok("Logout successful",null);
    }

    @Operation(summary = "Refresh access token", description = "Refresh access token using refresh token")
    @SecurityRequirements() // No bearer token required, uses refresh token in header
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(@RequestHeader("re-token") String refreshToken) {

        return ApiResponse.ok("Access token refreshed successfully", authService.refreshToken(refreshToken));
    }

    @Operation(description = "Verify email", summary = "Verify email and enable account")
    @SecurityRequirements() // No authentication required
    @GetMapping("/verify-email")
    public void verifyEmailAndEnableAccount(@RequestParam("token") String token,
                                            HttpServletResponse response) throws IOException {
        authService.verifyEmailAndEnableAccount(token);
//        response.sendRedirect("https://www.facebook.com");
    }

    @Operation(summary = "Resend verification email")
    @SecurityRequirements() // No authentication required
    @PostMapping("/resend-verify-email")
    public ApiResponse<Void> resendVerifyEmail(@RequestParam("email") String email) {
        authService.resendVerificationEmail(email);
        return ApiResponse.ok("Verification email sent again", null);
    }

    @Operation(summary = "Forgot password", description = "Send reset password email")
    @SecurityRequirements() // No authentication required
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestParam("email") String email) {
        authService.forgotPassword(email);
        return ApiResponse.ok("If the email exists, you will receive a email recovery guide.", null);
    }

    @Operation(summary = "Verify OTP", description = "Verify OTP code and get reset password token")
    @SecurityRequirements() // No authentication required
    @PostMapping("/verify-otp")
    public ApiResponse<VerifyOtpResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        return ApiResponse.ok("OTP verified successfully", authService.verifyOtp(request));
    }

    @Operation(summary = "Reset password", description = "Reset user password")
    @SecurityRequirements() // No authentication required
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successfully", null);
    }

    @Operation(summary = "Upload avatar", description = "Upload profile image for authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PostAuthorize("returnObject.data == authentication.name")
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(@RequestPart("avatar") MultipartFile avatar) {
        return ApiResponse.ok("Image uploaded successfully", authService.uploadImage(avatar));
    }
}
