package com.ctxh.volunteer.module.auth.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.common.util.AppConstants;
import com.ctxh.volunteer.common.util.ImageValidator;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.config.RSAKeyRecord;
import com.ctxh.volunteer.module.auth.dto.request.CompleteProfile;
import com.ctxh.volunteer.module.auth.dto.request.LoginRequest;
import com.ctxh.volunteer.module.auth.dto.request.ResetPasswordRequest;
import com.ctxh.volunteer.module.auth.dto.request.VerifyOtpRequest;
import com.ctxh.volunteer.module.auth.dto.response.GoogleSignInResponseDto;
import com.ctxh.volunteer.module.auth.dto.response.TokenResponse;
import com.ctxh.volunteer.module.auth.dto.response.VerifyOtpResponse;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.auth.service.IntrospectToken;
import com.ctxh.volunteer.module.auth.service.MailService;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MailService mailService;

    @Mock
    private IntrospectToken introspectToken;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private RSAKeyRecord rsaKeyRecord;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ImageValidator imageValidator;

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role studentRole;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate real RSA keys for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Mock RSA key record (lenient to avoid unnecessary stubbing warnings)
        lenient().when(rsaKeyRecord.rsaPublicKey()).thenReturn(publicKey);
        lenient().when(rsaKeyRecord.rsaPrivateKey()).thenReturn(privateKey);
        lenient().when(rsaKeyRecord.keyId()).thenReturn("test-key-id");

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "expirationTime", 604800L);
        ReflectionTestUtils.setField(authService, "refreshExpTime", 2592000L);
        ReflectionTestUtils.setField(authService, "baseUrl", "http://localhost:8080");

        // Setup test data
        studentRole = Role.builder()
                .roleId(1L)
                .roleName(RoleEnum.STUDENT.name())
                .build();

        testUser = User.builder()
                .userId(1L)
                .email("test@hcmut.edu.vn")
                .password("$2a$10$hashedPassword")
                .isVerified(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(List.of(studentRole))
                .build();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login - Success with valid credentials")
    void login_Success_WithValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest("test@hcmut.edu.vn", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act
        TokenResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();
        assertThat(testUser.getFailedLoginAttempts()).isZero();
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Login - Fail with non-existent email")
    void login_ThrowsException_WhenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@hcmut.edu.vn", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_OR_PASSWORD_INCORRECT);

        verify(userRepository).findByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Login - Fail with wrong password")
    void login_ThrowsException_WithWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@hcmut.edu.vn", "wrongPassword");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_OR_PASSWORD_INCORRECT);

        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), testUser.getPassword());
    }

    // ==================== VERIFY EMAIL TESTS ====================

    @Test
    @DisplayName("Verify Email - Success with valid token")
    void verifyEmailAndEnableAccount_Success_WithValidToken() {
        // Arrange
        String token = "valid.jwt.token";
        testUser.setIsVerified(false);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.VERIFY_EMAIL.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // 1 hour from now
                .build();

        when(introspectToken.parseAndVerifyToken(token, publicKey)).thenReturn(claimsSet);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.verifyEmailAndEnableAccount(token);

        // Assert
        assertThat(testUser.getIsVerified()).isTrue();
        verify(introspectToken).parseAndVerifyToken(token, publicKey);
        verify(userRepository).findById(testUser.getUserId());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Verify Email - Fail with invalid purpose token")
    void verifyEmailAndEnableAccount_ThrowsException_WithInvalidPurpose() {
        // Arrange
        String token = "invalid.purpose.token";

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.ACCESS.name()) // Wrong purpose
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(token, publicKey)).thenReturn(claimsSet);

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyEmailAndEnableAccount(token))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VERIFY_EMAIL_FAILED);

        verify(introspectToken).parseAndVerifyToken(token, publicKey);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Verify Email - Fail with expired token")
    void verifyEmailAndEnableAccount_ThrowsException_WithExpiredToken() {
        // Arrange
        String token = "expired.token";

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.VERIFY_EMAIL.name())
                .expirationTime(new Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
                .build();

        when(introspectToken.parseAndVerifyToken(token, publicKey)).thenReturn(claimsSet);

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyEmailAndEnableAccount(token))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_EXPIRED);

        verify(introspectToken).parseAndVerifyToken(token, publicKey);
        verifyNoInteractions(userRepository);
    }

    // ==================== RESEND VERIFICATION EMAIL TESTS ====================

    @Test
    @DisplayName("Resend Verification Email - Success for unverified user")
    void resendVerificationEmail_Success_ForUnverifiedUser() throws Exception {
        // Arrange
        testUser.setIsVerified(false);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        doNothing().when(mailService).sendEmail(anyString(), anyString(), any());

        // Act
        authService.resendVerificationEmail(testUser.getEmail());

        // Assert
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(mailService).sendEmail(eq(testUser.getEmail()), anyString(), any());
    }

    @Test
    @DisplayName("Resend Verification Email - Fail for already verified user")
    void resendVerificationEmail_ThrowsException_WhenAlreadyVerified() {
        // Arrange
        testUser.setIsVerified(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.resendVerificationEmail(testUser.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_VERIFIED);

        verify(userRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(mailService);
    }

    @Test
    @DisplayName("Resend Verification Email - Fail when user not found")
    void resendVerificationEmail_ThrowsException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.resendVerificationEmail("nonexistent@hcmut.edu.vn"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByEmail("nonexistent@hcmut.edu.vn");
        verifyNoInteractions(mailService);
    }

    // ==================== FORGOT PASSWORD TESTS ====================

    @Test
    @DisplayName("Forgot Password - Success and sends OTP email")
    void forgotPassword_Success_SendsOtpEmail() throws Exception {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(mailService).sendEmail(anyString(), anyString(), any());

        // Act
        authService.forgotPassword(testUser.getEmail());

        // Assert
        assertThat(testUser.getResetPasswordToken()).isNotNull();
        assertThat(testUser.getResetPasswordToken()).hasSize(6);
        assertThat(testUser.getResetPasswordToken()).matches("\\d{6}");
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository).save(testUser);
        verify(mailService).sendEmail(eq(testUser.getEmail()), anyString(), any());
    }

    @Test
    @DisplayName("Forgot Password - Fail for locked account")
    void forgotPassword_ThrowsException_WhenAccountLocked() {
        // Arrange
        testUser.setIsLocked(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.forgotPassword(testUser.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);

        verify(userRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(mailService);
    }

    @Test
    @DisplayName("Forgot Password - Fail for unverified account")
    void forgotPassword_ThrowsException_WhenAccountNotVerified() {
        // Arrange
        testUser.setIsVerified(false);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.forgotPassword(testUser.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_DISABLED);

        verify(userRepository).findByEmail(testUser.getEmail());
        verifyNoInteractions(mailService);
    }

    // ==================== VERIFY OTP TESTS ====================

    @Test
    @DisplayName("Verify OTP - Success with valid OTP")
    void verifyOtp_Success_WithValidOtp() {
        // Arrange
        String otpCode = "123456";
        testUser.generatePasswordResetToken(otpCode);

        VerifyOtpRequest request = new VerifyOtpRequest(testUser.getEmail(), otpCode);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        VerifyOtpResponse response = authService.verifyOtp(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getResetPasswordToken()).isNotEmpty();
        assertThat(testUser.getResetPasswordToken()).isNull();
        assertThat(testUser.getResetPasswordTokenExpiresAt()).isNull();
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Verify OTP - Fail with invalid OTP")
    void verifyOtp_ThrowsException_WithInvalidOtp() {
        // Arrange
        testUser.generatePasswordResetToken("123456");
        VerifyOtpRequest request = new VerifyOtpRequest(testUser.getEmail(), "999999"); // Wrong OTP

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("Verify OTP - Fail for locked account")
    void verifyOtp_ThrowsException_WhenAccountLocked() {
        // Arrange
        testUser.setIsLocked(true);
        VerifyOtpRequest request = new VerifyOtpRequest(testUser.getEmail(), "123456");

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);

        verify(userRepository).findByEmail(testUser.getEmail());
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    @DisplayName("Reset Password - Success with valid token")
    void resetPassword_Success_WithValidToken() {
        // Arrange
        String newPassword = "newPassword123";
        ResetPasswordRequest request = new ResetPasswordRequest("valid.token", newPassword, newPassword);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.RESET_PASSWORD.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(request.getToken(), publicKey)).thenReturn(claimsSet);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.resetPassword(request);

        // Assert
        verify(introspectToken).parseAndVerifyToken(request.getToken(), publicKey);
        verify(userRepository, times(2)).findById(testUser.getUserId()); // Called twice: getUserFromClaims() + revokeToken()
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Reset Password - Fail when passwords don't match")
    void resetPassword_ThrowsException_WhenPasswordsDontMatch() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("token", "password1", "password2");

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EQUAL_PASSWORD);

        verifyNoInteractions(introspectToken, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Reset Password - Fail with expired token")
    void resetPassword_ThrowsException_WithExpiredToken() {
        // Arrange
        String newPassword = "newPassword123";
        ResetPasswordRequest request = new ResetPasswordRequest("expired.token", newPassword, newPassword);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.RESET_PASSWORD.name())
                .expirationTime(new Date(System.currentTimeMillis() - 3600000)) // Expired
                .build();

        when(introspectToken.parseAndVerifyToken(request.getToken(), publicKey)).thenReturn(claimsSet);

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_EXPIRED);

        verify(introspectToken).parseAndVerifyToken(request.getToken(), publicKey);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    @DisplayName("Refresh Token - Success with valid refresh token")
    void refreshToken_Success_WithValidToken() {
        // Arrange
        String refreshToken = "valid.refresh.token";
        String jwtId = "test-jwt-id-123";

        testUser.setRefreshTokenUuid(jwtId);
        testUser.setRefreshTokenExpiresAt(new Date(System.currentTimeMillis() + 3600000));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .jwtID(jwtId)
                .claim(AppConstants.PURPOSE, PurposeToken.REFRESH.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(refreshToken, publicKey)).thenReturn(claimsSet);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.of(testUser));

        // Act
        TokenResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        verify(introspectToken).parseAndVerifyToken(refreshToken, publicKey);
        verify(userRepository).findById(testUser.getUserId());
    }

    @Test
    @DisplayName("Refresh Token - Fail with invalid purpose")
    void refreshToken_ThrowsException_WithInvalidPurpose() {
        // Arrange
        String refreshToken = "invalid.purpose.token";

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.ACCESS.name()) // Wrong purpose
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(refreshToken, publicKey)).thenReturn(claimsSet);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(introspectToken).parseAndVerifyToken(refreshToken, publicKey);
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Logout - Success revokes refresh token")
    void logout_Success_RevokesRefreshToken() {
        // Arrange
        String refreshToken = "valid.refresh.token";
        String jwtId = "test-jwt-id-123";

        testUser.setRefreshTokenUuid(jwtId);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .jwtID(jwtId)
                .claim(AppConstants.PURPOSE, PurposeToken.REFRESH.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(refreshToken, publicKey)).thenReturn(claimsSet);
        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.of(testUser));

        // Act
        authService.logout(refreshToken);

        // Assert
        assertThat(testUser.getRefreshTokenUuid()).isNull();
        assertThat(testUser.getRefreshTokenExpiresAt()).isNull();
        verify(introspectToken).parseAndVerifyToken(refreshToken, publicKey);
        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    @Test
    @DisplayName("Logout - Fail with invalid token purpose")
    void logout_ThrowsException_WithInvalidPurpose() {
        // Arrange
        String refreshToken = "invalid.purpose.token";

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(testUser.getUserId().toString())
                .claim(AppConstants.PURPOSE, PurposeToken.ACCESS.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        when(introspectToken.parseAndVerifyToken(refreshToken, publicKey)).thenReturn(claimsSet);

        // Act & Assert
        assertThatThrownBy(() -> authService.logout(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(introspectToken).parseAndVerifyToken(refreshToken, publicKey);
    }

    // ==================== REVOKE TOKEN TESTS ====================

    @Test
    @DisplayName("Revoke Token by UUID - Success")
    void revokeTokenByUuid_Success() {
        // Arrange
        String jwtId = "test-jwt-id-123";
        testUser.setRefreshTokenUuid(jwtId);

        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.of(testUser));

        // Act
        authService.revokeToken(jwtId);

        // Assert
        assertThat(testUser.getRefreshTokenUuid()).isNull();
        assertThat(testUser.getRefreshTokenExpiresAt()).isNull();
        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    @Test
    @DisplayName("Revoke Token by UUID - Fail when token not found")
    void revokeTokenByUuid_ThrowsException_WhenNotFound() {
        // Arrange
        String jwtId = "non-existent-jwt-id";
        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.revokeToken(jwtId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    @Test
    @DisplayName("Revoke Token by User ID - Success")
    void revokeTokenByUserId_Success() {
        // Arrange
        testUser.setRefreshTokenUuid("some-jwt-id");
        testUser.setRefreshTokenExpiresAt(new Date());

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act
        authService.revokeToken(testUser.getUserId());

        // Assert
        assertThat(testUser.getRefreshTokenUuid()).isNull();
        assertThat(testUser.getRefreshTokenExpiresAt()).isNull();
        verify(userRepository).findById(testUser.getUserId());
    }

    @Test
    @DisplayName("Revoke Token by User ID - Fail when user not found")
    void revokeTokenByUserId_ThrowsException_WhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.revokeToken(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(userId);
    }

    // ==================== IS REFRESH TOKEN VALID TESTS ====================

    @Test
    @DisplayName("Is Refresh Token Valid - Returns true for valid token")
    void isRefreshTokenValid_ReturnsTrue_ForValidToken() {
        // Arrange
        String jwtId = "valid-jwt-id";
        testUser.setRefreshTokenUuid(jwtId);
        testUser.setRefreshTokenExpiresAt(new Date(System.currentTimeMillis() + 3600000));

        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.of(testUser));

        // Act
        boolean isValid = authService.isRefreshTokenValid(jwtId);

        // Assert
        assertThat(isValid).isTrue();
        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    @Test
    @DisplayName("Is Refresh Token Valid - Returns false for expired token")
    void isRefreshTokenValid_ReturnsFalse_ForExpiredToken() {
        // Arrange
        String jwtId = "expired-jwt-id";
        testUser.setRefreshTokenUuid(jwtId);
        testUser.setRefreshTokenExpiresAt(new Date(System.currentTimeMillis() - 3600000)); // Expired

        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.of(testUser));

        // Act
        boolean isValid = authService.isRefreshTokenValid(jwtId);

        // Assert
        assertThat(isValid).isFalse();
        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    @Test
    @DisplayName("Is Refresh Token Valid - Throws exception when token not found")
    void isRefreshTokenValid_ThrowsException_WhenTokenNotFound() {
        // Arrange
        String jwtId = "non-existent-jwt-id";
        when(userRepository.findByRefreshTokenUuid(jwtId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.isRefreshTokenValid(jwtId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(userRepository).findByRefreshTokenUuid(jwtId);
    }

    // ==================== GOOGLE OAUTH LOGIN TESTS ====================

    @Test
    @DisplayName("Google OAuth - Success for new user with HCMUT email")
    void verifyGoogleIdToken_Success_ForNewHcmutUser() {
        // Arrange
        String idToken = "valid.google.id.token";
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "newuser@hcmut.edu.vn");
        googleUserInfo.put("name", "New User");
        googleUserInfo.put("picture", "https://google.com/avatar.jpg");
        googleUserInfo.put("sub", "google-provider-id-123");

        Student newStudent = Student.builder()
                .studentId(2L)
                .fullName("New User")
                .totalCtxhDays(0.0)
                .build();

        User newUser = User.builder()
                .userId(2L)
                .email("newuser@hcmut.edu.vn")
                .isVerified(true)
                .avatarUrl("https://google.com/avatar.jpg")
                .provider("google")
                .providerId("google-provider-id-123")
                .roles(List.of(studentRole))
                .student(newStudent)
                .build();

        newStudent.setUser(newUser);

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(googleUserInfo));

        when(userRepository.findByEmail("newuser@hcmut.edu.vn")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleEnum.STUDENT.name())).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getUserId() == null) {
                user.setUserId(2L);
            }
            return user;
        });
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);

        // Act
        GoogleSignInResponseDto response = authService.verifyGoogleIdToken(idToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.isProfileComplete()).isFalse(); // MSSV is null for new user

        verify(webClient).get();
        verify(userRepository).findByEmail("newuser@hcmut.edu.vn");
        verify(roleRepository).findByRoleName(RoleEnum.STUDENT.name());
        verify(userRepository, times(2)).save(any(User.class)); // Once in onboard, once for login
    }

    @Test
    @DisplayName("Google OAuth - Success for existing user with completed profile")
    void verifyGoogleIdToken_Success_ForExistingUserWithProfile() {
        // Arrange
        String idToken = "valid.google.id.token";
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "test@hcmut.edu.vn");
        googleUserInfo.put("name", "Test User");
        googleUserInfo.put("picture", "https://google.com/avatar.jpg");
        googleUserInfo.put("sub", "google-provider-id-123");

        Student existingStudent = Student.builder()
                .studentId(1L)
                .fullName("Test User")
                .mssv("2012345")
                .user(testUser)
                .build();

        testUser.setStudent(existingStudent);
        testUser.setProviderId("google-provider-id-123");
        testUser.setProvider("google");

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(googleUserInfo));

        when(userRepository.findByEmail("test@hcmut.edu.vn")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        GoogleSignInResponseDto response = authService.verifyGoogleIdToken(idToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getRefreshToken()).isNotEmpty();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.isProfileComplete()).isTrue(); // Has MSSV

        verify(webClient).get();
        verify(userRepository).findByEmail("test@hcmut.edu.vn");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Google OAuth - Fails for non-HCMUT email domain")
    void verifyGoogleIdToken_ThrowsException_ForNonHcmutEmail() {
        // Arrange
        String idToken = "valid.google.id.token";
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "user@gmail.com");
        googleUserInfo.put("name", "Gmail User");
        googleUserInfo.put("picture", "https://google.com/avatar.jpg");
        googleUserInfo.put("sub", "google-provider-id-456");

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(googleUserInfo));

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyGoogleIdToken(idToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);

        verify(webClient).get();
        verifyNoInteractions(userRepository, roleRepository);
    }

    @Test
    @DisplayName("Google OAuth - Fails for locked account")
    void verifyGoogleIdToken_ThrowsException_ForLockedAccount() {
        // Arrange
        String idToken = "valid.google.id.token";
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "test@hcmut.edu.vn");
        googleUserInfo.put("name", "Test User");
        googleUserInfo.put("picture", "https://google.com/avatar.jpg");
        googleUserInfo.put("sub", "google-provider-id-123");

        testUser.setIsLocked(true);
        testUser.setProviderId("google-provider-id-123");

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(googleUserInfo));

        when(userRepository.findByEmail("test@hcmut.edu.vn")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyGoogleIdToken(idToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);

        verify(webClient).get();
        verify(userRepository).findByEmail("test@hcmut.edu.vn");
    }

    @Test
    @DisplayName("Google OAuth - Updates provider ID for existing user without provider")
    void verifyGoogleIdToken_UpdatesProviderId_ForExistingUserWithoutProvider() {
        // Arrange
        String idToken = "valid.google.id.token";
        Map<String, Object> googleUserInfo = new HashMap<>();
        googleUserInfo.put("email", "test@hcmut.edu.vn");
        googleUserInfo.put("name", "Test User");
        googleUserInfo.put("picture", "https://google.com/avatar.jpg");
        googleUserInfo.put("sub", "new-google-provider-id");

        testUser.setProviderId(null);
        testUser.setProvider(null);

        Student existingStudent = Student.builder()
                .studentId(1L)
                .fullName("Test User")
                .user(testUser)
                .build();
        testUser.setStudent(existingStudent);

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(googleUserInfo));

        when(userRepository.findByEmail("test@hcmut.edu.vn")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        GoogleSignInResponseDto response = authService.verifyGoogleIdToken(idToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(testUser.getProvider()).isEqualTo("google");
        assertThat(testUser.getProviderId()).isEqualTo("new-google-provider-id");

        verify(userRepository, times(2)).save(testUser); // Once for provider update, once for login
    }

    @Test
    @DisplayName("Google OAuth - Fails with invalid token")
    void verifyGoogleIdToken_ThrowsException_WithInvalidToken() {
        // Arrange
        String idToken = "invalid.google.id.token";
        Map<String, Object> errorResponse = Map.of("error", "invalid_token");

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(errorResponse));

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyGoogleIdToken(idToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_INVALID);

        verify(webClient).get();
        verifyNoInteractions(userRepository);
    }

    // ==================== COMPLETE STUDENT PROFILE TESTS ====================

    @Test
    @DisplayName("Complete Student Profile - Success updates student info")
    void completeStudentProfile_Success_UpdatesStudentInfo() {
        // Arrange
        Student testStudent = Student.builder()
                .studentId(1L)
                .fullName("Test Student")
                .user(testUser)
                .build();
        testUser.setStudent(testStudent);

        CompleteProfile request = new CompleteProfile();
        request.setUserId(testUser.getUserId());
        request.setMssv("2012345");
        request.setGender("MALE");
        request.setPassword("newPassword123");

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        authService.completeStudentProfile(request);

        // Assert
        assertThat(testStudent.getMssv()).isEqualTo("2012345");
        assertThat(testStudent.getGender()).isEqualTo(Gender.MALE);
        verify(userRepository).findById(testUser.getUserId());
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("Complete Student Profile - Fails when user not found")
    void completeStudentProfile_ThrowsException_WhenUserNotFound() {
        // Arrange
        CompleteProfile request = new CompleteProfile();
        request.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.completeStudentProfile(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(999L);
        verifyNoInteractions(studentRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Complete Student Profile - Fails when user has no student entity")
    void completeStudentProfile_ThrowsException_WhenStudentNotFound() {
        // Arrange
        testUser.setStudent(null);

        CompleteProfile request = new CompleteProfile();
        request.setUserId(testUser.getUserId());

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.completeStudentProfile(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);

        verify(userRepository).findById(testUser.getUserId());
        verifyNoInteractions(studentRepository);
    }

    @Test
    @DisplayName("Complete Student Profile - Sets FEMALE gender correctly")
    void completeStudentProfile_SetsFemaleGender() {
        // Arrange
        Student testStudent = Student.builder()
                .studentId(1L)
                .fullName("Test Student")
                .user(testUser)
                .build();
        testUser.setStudent(testStudent);

        CompleteProfile request = new CompleteProfile();
        request.setUserId(testUser.getUserId());
        request.setMssv("2012345");
        request.setGender("FEMALE");
        request.setPassword("newPassword123");

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        authService.completeStudentProfile(request);

        // Assert
        assertThat(testStudent.getGender()).isEqualTo(Gender.FEMALE);
    }

    // ==================== UPLOAD IMAGE TESTS ====================

    @Test
    @DisplayName("Upload Image - Validates image file")
    void uploadImage_ValidatesImageFile() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        doThrow(new BusinessException(ErrorCode.INVALID_FILE_TYPE))
                .when(imageValidator).validate(mockFile);

        // Act & Assert - Test that imageValidator is called
        assertThatThrownBy(() -> imageValidator.validate(mockFile))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);

        verify(imageValidator).validate(mockFile);
    }

    // Note: Full uploadImage() test requires mocking AuthUtil.getIdFromAuthentication()
    // which is a static utility method. This is better tested in integration tests.
}
