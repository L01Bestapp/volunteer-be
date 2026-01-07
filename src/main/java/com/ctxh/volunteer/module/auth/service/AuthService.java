package com.ctxh.volunteer.module.auth.service;


import com.ctxh.volunteer.module.auth.dto.request.CompleteProfile;
import com.ctxh.volunteer.module.auth.dto.request.LoginRequest;
import com.ctxh.volunteer.module.auth.dto.request.ResetPasswordRequest;
import com.ctxh.volunteer.module.auth.dto.request.VerifyOtpRequest;
import com.ctxh.volunteer.module.auth.dto.response.GoogleSignInResponseDto;
import com.ctxh.volunteer.module.auth.dto.response.TokenResponse;
import com.ctxh.volunteer.module.auth.dto.response.VerifyOtpResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    void verifyEmailAndEnableAccount(String token);
    void resendVerificationEmail(String email);
    void forgotPassword(String email);
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
    TokenResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void revokeToken(String uuidToken);
    void revokeToken(Long userId);
    boolean isRefreshTokenValid(String uuidToken);
    void completeStudentProfile(CompleteProfile request);
    String uploadImage(MultipartFile file);
    GoogleSignInResponseDto verifyGoogleIdToken(String idToken);
}
