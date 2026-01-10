package com.ctxh.volunteer.module.auth.service.impl;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.common.util.AppConstants;
import com.ctxh.volunteer.common.util.AuthUtil;
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
import com.ctxh.volunteer.module.auth.enums.EmailTemplates;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.auth.service.AuthService;
import com.ctxh.volunteer.module.auth.service.IntrospectToken;
import com.ctxh.volunteer.module.auth.service.MailService;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import io.hypersistence.tsid.TSID;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final IntrospectToken introspectToken;
    private final WebClient webClient;
    private final RSAKeyRecord rsaKeyRecord;
    private final StudentRepository studentRepository;
    private final ImageValidator imageValidator;
    private final Cloudinary cloudinary;


    @Value("${jwt.expirationTime}")
    @NonFinal
    private long expirationTime;

    @Value("${jwt.refreshExpTime}")
    @NonFinal
    private long refreshExpTime;

    @Value("${base.url}")
    private String baseUrl;


    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.EMAIL_OR_PASSWORD_INCORRECT)
        );
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            throw new BusinessException(ErrorCode.EMAIL_OR_PASSWORD_INCORRECT);
        }

        // Here you would typically generate a JWT token or similar
        String accessToken = generateToken(user, PurposeToken.ACCESS);
        String refreshToken = generateToken(user, PurposeToken.REFRESH);
        String[] roles = user.getRoles().stream().map(Role::getRoleName).toArray(String[]::new);
        String role = roles[0];
        if (roles.length == 3) role = "ADMIN";

        // reset failed login attempts
        user.setFailedLoginAttempts(0);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }

    @Override
    @Transactional
    public void verifyEmailAndEnableAccount(String token) {
        JWTClaimsSet claimsSet = introspectToken.parseAndVerifyToken(token, rsaKeyRecord.rsaPublicKey());
        if (claimsSet == null || !PurposeToken.VERIFY_EMAIL.name().equals(claimsSet.getClaim(AppConstants.PURPOSE))) {
            throw new BusinessException(ErrorCode.VERIFY_EMAIL_FAILED);
        }

        if (claimsSet.getExpirationTime().before(new Date())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        Long userId = Long.valueOf(claimsSet.getSubject());
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        if (Boolean.FALSE.equals(user.getIsVerified()))
            user.setIsVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_VERIFIED);
        }
        sendEmailVerification(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(
                user -> {
                    if (Boolean.TRUE.equals(user.getIsLocked())) {
                        throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
                    }
                    if (Boolean.FALSE.equals(user.getIsVerified())) {
                        throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
                    }
                    // Generate random 6-digit OTP code
                    String otpCode = String.format("%06d", (int)(Math.random() * 1000000));

                    // Save OTP to database using User entity method
                    user.generatePasswordResetToken(otpCode);
                    userRepository.save(user);

                    try {
                        mailService.sendEmail(user.getEmail(), otpCode, EmailTemplates.VERIFY_RESET_PASSWORD_TEMPLATE);
                        log.info("Sent reset password OTP to {}", user.getEmail());
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        log.error("Failed to send reset password email", e);
                        throw new BusinessException(ErrorCode.MAIL_SENDING_FAILED);
                    }
                }
        );
    }

    @Override
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Begin verify OTP for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (Boolean.FALSE.equals(user.getIsVerified())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // Verify OTP code and expiration
        if (!user.isPasswordResetTokenValid()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!request.getOtpCode().equals(user.getResetPasswordToken())) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // Generate reset password token (JWT)
        String resetPasswordToken = generateToken(user, PurposeToken.RESET_PASSWORD);

        // Clear OTP from database (one-time use)
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);
        userRepository.save(user);

        log.info("OTP verified successfully for email: {}", request.getEmail());

        return VerifyOtpResponse.builder()
                .resetPasswordToken(resetPasswordToken)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("begin reset password");
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_PASSWORD);
        }
        JWTClaimsSet claimsSet = introspectToken.parseAndVerifyToken(request.getToken(), rsaKeyRecord.rsaPublicKey());
        if (claimsSet == null || !PurposeToken.RESET_PASSWORD.name().equals(claimsSet.getClaim(AppConstants.PURPOSE))) {
            throw new BusinessException(ErrorCode.ERROR_RESET_PASSWORD);
        }

        if (claimsSet.getExpirationTime().before(new Date())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = getUserFromClaims(claimsSet);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        if (Boolean.FALSE.equals(user.getIsVerified())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            user.setIsLocked(false);
        }
        userRepository.save(user);
        // revoke all refresh tokens for this user
        revokeToken(user.getUserId());
        log.info("complete reset password for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        log.info("begin refresh Token");

        JWTClaimsSet claimsSet = introspectToken.parseAndVerifyToken(refreshToken, rsaKeyRecord.rsaPublicKey());
        if (claimsSet == null || !PurposeToken.REFRESH.name().equals(claimsSet.getClaim(AppConstants.PURPOSE))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        if (claimsSet.getExpirationTime().before(new Date())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = getUserFromClaims(claimsSet);

        // check it into db
        boolean refreshTokenValid = isRefreshTokenValid(claimsSet.getJWTID());
        if (!refreshTokenValid) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        // generate a new access token
        String accessToken = generateToken(user, PurposeToken.ACCESS);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        JWTClaimsSet claimsSet = introspectToken.parseAndVerifyToken(refreshToken, rsaKeyRecord.rsaPublicKey());
        if (claimsSet == null || !PurposeToken.REFRESH.name().equals(claimsSet.getClaim(AppConstants.PURPOSE))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        revokeToken(claimsSet.getJWTID());
    }

    @Override
    public void revokeToken(String uuidToken) {
        User user = userRepository.findByRefreshTokenUuid(uuidToken).orElseThrow(
                () -> new BusinessException(ErrorCode.TOKEN_INVALID
        ));
        user.setRefreshTokenUuid(null);
        user.setRefreshTokenExpiresAt(null);
    }

    @Override
    public void revokeToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND
        ));
        user.setRefreshTokenUuid(null);
        user.setRefreshTokenExpiresAt(null);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isRefreshTokenValid(String uuidToken) {
        User user = userRepository.findByRefreshTokenUuid(uuidToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));
        return !user.getRefreshTokenExpiresAt().before(new Date());

    }

    @Override
    @Transactional
    public void completeStudentProfile(CompleteProfile request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        Student student = user.getStudent();
        if (student == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        student.setGender(Gender.valueOf(request.getGender()));
        student.setMssv(request.getMssv());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public String uploadImage(MultipartFile avatar) {
        User user = getCurrentUser();
        imageValidator.validate(avatar);
        try {
            Map<?,?> uploadResult = cloudinary.uploader().upload(avatar.getBytes(), ObjectUtils.emptyMap());
            log.info("Uploaded image to Cloudinary: {}", uploadResult);
            String avatarUrl = uploadResult.get("secure_url").toString();
            user.setAvatarUrl(avatarUrl);
            // because of @Transactional, no need to call save explicitly
            return user.getUserId().toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    @Override
    @Transactional
    public GoogleSignInResponseDto verifyGoogleIdToken(String idToken) {
        log.info("Begin verify Google ID token");

        // 1. Verify ID Token với Google
        Map<String, Object> googleUserInfo = verifyTokenWithGoogle(idToken);

        // 2. Extract user info từ token payload
        String email = (String) googleUserInfo.get("email");
        String name = (String) googleUserInfo.get("name");
        String picture = (String) googleUserInfo.get("picture");
        String providerId = (String) googleUserInfo.get("sub");

        // 3. Kiểm tra email có phải @hcmut.edu.vn không
        if (!email.endsWith("@hcmut.edu.vn")) {
            log.warn("Email {} is not from HCMUT domain", email);
            throw new BusinessException(ErrorCode.EMAIL_DOMAIN_NOT_ALLOWED);
        }

        // 4. Kiểm tra user đã tồn tại chưa
        User user = userRepository.findByEmail(email).orElse(null);

        boolean isNewUser = false;
        if (user == null) {
            // Onboard user nếu chưa tồn tại
            log.info("User with email {} does not exist, onboarding...", email);
            user = onBoardUserFromOauth2(email, name, picture, "google", providerId);
            isNewUser = true;
        } else {
            // Nếu user đã tồn tại nhưng chưa có providerId (đăng ký bằng email trước đó)
            // thì cập nhật providerId
            if (user.getProviderId() == null || user.getProviderId().isEmpty()) {
                user.setProvider("google");
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        }

        // 5. Kiểm tra account status
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 6. Generate JWT tokens
        String accessToken = generateToken(user, PurposeToken.ACCESS);
        String refreshToken = generateToken(user, PurposeToken.REFRESH);

        // 7. Update last login
        user.recordSuccessfulLogin();
        userRepository.save(user);

        log.info("Google sign-in successful for user: {}", email);

        // 8. Check if profile is complete (có MSSV chưa)
        boolean profileComplete = user.getStudent() != null && user.getStudent().getMssv() != null;

        return GoogleSignInResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .profileComplete(profileComplete)
                .build();
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        user.banUser();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unBanUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        user.unBanUser();
        userRepository.save(user);
    }


    private Map<String, Object> verifyTokenWithGoogle(String idToken) {
        try {
            String googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            Map<String, Object> response = webClient.get()
                    .uri(googleTokenInfoUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.containsKey("error")) {
                log.error("Invalid Google ID token");
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }

            return response;
        } catch (Exception e) {
            log.error("Failed to verify Google ID token", e);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User onBoardUserFromOauth2(String email, String fullName, String avatarUrl, String provider, String providerId) {
        Role role = roleRepository.findByRoleName(RoleEnum.STUDENT.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );

        User user = User.builder()
                .email(email)
                .isVerified(true)
                .avatarUrl(avatarUrl)
                .provider(provider)
                .roles(List.of(role))
                .providerId(providerId)
                .build();

        // Create student
        Student student = Student.builder()
                .user(user)
                .fullName(fullName)
                .totalCtxhDays(0.0)
                .build();

        user.setStudent(student);
        userRepository.save(user);
        Student savedStudent = studentRepository.save(student);
        log.info("Created student with ID: {}", savedStudent.getStudentId());

        return user;
    }

    public void sendEmailVerification(User user) {
        String tokenVerify = generateToken(user, PurposeToken.VERIFY_EMAIL);
        String linkConfirm = baseUrl + "/api/v1" + "/auth/verify-email?token=" + tokenVerify;
        try {
            mailService.sendEmail(user.getEmail(), linkConfirm, EmailTemplates.VERIFY_EMAIL_TEMPLATE);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email verification", e);
            throw new BusinessException(ErrorCode.MAIL_SENDING_FAILED);
        }
    }

    public String generateToken(User user, PurposeToken purpose) {
        // Header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKeyRecord.keyId())
                .type(JOSEObjectType.JWT)
                .build();
        // Payload
        long expireSeconds = getExpirationSeconds(purpose);
        String jwtId = TSID.Factory.getTsid().toString();

        JWTClaimsSet claimsSet = buildClaims(user, purpose, jwtId, expireSeconds);
        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject object = new JWSObject(header, payload);
        // Signature
        try {
            if (rsaKeyRecord.rsaPrivateKey() == null) {
                throw new IllegalStateException("Private key is not initialized");
            }
            JWSSigner signer = new RSASSASigner(rsaKeyRecord.rsaPrivateKey());
            object.sign(signer);
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new BusinessException(ErrorCode.TOKEN_GENERATION_FAILED);
        }

        // save refresh token if purpose is REFRESH
        if (purpose == PurposeToken.REFRESH) {
            user.setRefreshTokenUuid(jwtId);
            user.setRefreshTokenExpiresAt(claimsSet.getExpirationTime());
        }
        return object.serialize();
    }

    private JWTClaimsSet buildClaims(User user, PurposeToken purpose, String jwtId, long expireSeconds) {
        return new JWTClaimsSet.Builder()
                .issuer("ctxh.com")
                .subject(user.getUserId().toString())
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(expireSeconds)))
                .jwtID(jwtId)
                .claim("scope", buildScopes(user.getRoles()))
                .claim(AppConstants.PURPOSE, purpose)
                .audience("ctxh.com")
                .build();
    }


    private long getExpirationSeconds(PurposeToken purpose) {
        return switch (purpose) {
            case ACCESS -> expirationTime;
            case VERIFY_EMAIL -> 86400; // 1 day
            case RESET_PASSWORD -> 30 * 60; // 30 minutes
            case REFRESH -> refreshExpTime;
        };
    }

    private List<String> buildScopes(List<Role> roles) {
        return roles.stream().map(Role::getRoleName).toList();

    }

    public User getUserFromClaims(JWTClaimsSet claimsSet) {
        Long userId = Long.valueOf(claimsSet.getSubject());
        return userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
    }

    public User getCurrentUser() {
        Long userId = AuthUtil.getIdFromAuthentication();
        return userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
    }
}
