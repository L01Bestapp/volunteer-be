package com.ctxh.volunteer.module.auth.service.impl;

import com.ctxh.volunteer.common.util.AppConstants;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IntrospectToken Unit Tests")
class IntrospectTokenImplTest {

    private IntrospectTokenImpl introspectToken;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        introspectToken = new IntrospectTokenImpl();

        // Generate real RSA keys for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Test
    @DisplayName("Verify Token - Returns true for valid access token")
    void verifyToken_ReturnsTrue_ForValidAccessToken() throws Exception {
        // Arrange
        String token = generateValidToken(PurposeToken.ACCESS, System.currentTimeMillis() + 3600000);

        // Act
        boolean isValid = introspectToken.verifyToken(token, publicKey, PurposeToken.ACCESS);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Verify Token - Returns false for expired token")
    void verifyToken_ReturnsFalse_ForExpiredToken() throws Exception {
        // Arrange
        String token = generateValidToken(PurposeToken.ACCESS, System.currentTimeMillis() - 3600000); // Expired 1 hour ago

        // Act
        boolean isValid = introspectToken.verifyToken(token, publicKey, PurposeToken.ACCESS);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Verify Token - Returns false for wrong purpose")
    void verifyToken_ReturnsFalse_ForWrongPurpose() throws Exception {
        // Arrange
        String token = generateValidToken(PurposeToken.ACCESS, System.currentTimeMillis() + 3600000);

        // Act - Expecting REFRESH but token is ACCESS
        boolean isValid = introspectToken.verifyToken(token, publicKey, PurposeToken.REFRESH);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Verify Token - Returns false for invalid signature")
    void verifyToken_ReturnsFalse_ForInvalidSignature() throws Exception {
        // Arrange - Generate token with different key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair differentKeyPair = keyGen.generateKeyPair();
        RSAPrivateKey differentPrivateKey = (RSAPrivateKey) differentKeyPair.getPrivate();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("1")
                .claim(AppConstants.PURPOSE, PurposeToken.ACCESS.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        JWSSigner signer = new RSASSASigner(differentPrivateKey);
        jwsObject.sign(signer);

        String token = jwsObject.serialize();

        // Act - Verify with different public key
        boolean isValid = introspectToken.verifyToken(token, publicKey, PurposeToken.ACCESS);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Verify Token - Returns false for malformed token")
    void verifyToken_ReturnsFalse_ForMalformedToken() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act
        boolean isValid = introspectToken.verifyToken(malformedToken, publicKey, PurposeToken.ACCESS);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Verify Token - Validates refresh token correctly")
    void verifyToken_Success_ForRefreshToken() throws Exception {
        // Arrange
        String token = generateValidToken(PurposeToken.REFRESH, System.currentTimeMillis() + 2592000000L); // 30 days

        // Act
        boolean isValid = introspectToken.verifyToken(token, publicKey, PurposeToken.REFRESH);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Parse And Verify Token - Returns claims for valid token")
    void parseAndVerifyToken_ReturnsClaims_ForValidToken() throws Exception {
        // Arrange
        String userId = "123";
        String token = generateValidTokenWithUserId(userId, PurposeToken.ACCESS);

        // Act
        JWTClaimsSet claimsSet = introspectToken.parseAndVerifyToken(token, publicKey);

        // Assert
        assertThat(claimsSet).isNotNull();
        assertThat(claimsSet.getSubject()).isEqualTo(userId);
        assertThat(claimsSet.getClaim(AppConstants.PURPOSE)).isEqualTo(PurposeToken.ACCESS.name());
        assertThat(claimsSet.getIssuer()).isEqualTo("ctxh.com");
        assertThat(claimsSet.getAudience()).containsExactly("ctxh.com");
    }

    @Test
    @DisplayName("Parse And Verify Token - Returns null for invalid signature")
    void parseAndVerifyToken_ReturnsNull_ForInvalidSignature() throws Exception {
        // Arrange - Generate token with different key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair differentKeyPair = keyGen.generateKeyPair();
        RSAPrivateKey differentPrivateKey = (RSAPrivateKey) differentKeyPair.getPrivate();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("1")
                .claim(AppConstants.PURPOSE, PurposeToken.ACCESS.name())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        JWSSigner signer = new RSASSASigner(differentPrivateKey);
        jwsObject.sign(signer);

        String token = jwsObject.serialize();

        // Act
        JWTClaimsSet result = introspectToken.parseAndVerifyToken(token, publicKey);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Parse And Verify Token - Returns null for malformed token")
    void parseAndVerifyToken_ReturnsNull_ForMalformedToken() {
        // Arrange
        String malformedToken = "malformed.token.here";

        // Act
        JWTClaimsSet result = introspectToken.parseAndVerifyToken(malformedToken, publicKey);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Parse And Verify Token - Correctly extracts all claims")
    void parseAndVerifyToken_ExtractsAllClaims() throws Exception {
        // Arrange
        String userId = "456";
        String jwtId = "test-jwt-id-123";

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("ctxh.com")
                .subject(userId)
                .audience("ctxh.com")
                .jwtID(jwtId)
                .issueTime(Date.from(Instant.now()))
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim(AppConstants.PURPOSE, PurposeToken.REFRESH.name())
                .claim("scope", "STUDENT")
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("test-key")
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        JWSSigner signer = new RSASSASigner(privateKey);
        jwsObject.sign(signer);

        String token = jwsObject.serialize();

        // Act
        JWTClaimsSet result = introspectToken.parseAndVerifyToken(token, publicKey);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo(userId);
        assertThat(result.getJWTID()).isEqualTo(jwtId);
        assertThat(result.getIssuer()).isEqualTo("ctxh.com");
        assertThat(result.getAudience()).containsExactly("ctxh.com");
        assertThat(result.getClaim(AppConstants.PURPOSE)).isEqualTo(PurposeToken.REFRESH.name());
        assertThat(result.getClaim("scope")).isEqualTo("STUDENT");
    }

    // ==================== HELPER METHODS ====================

    private String generateValidToken(PurposeToken purpose, long expirationTimeMillis) throws Exception {
        return generateValidTokenWithUserId("1", purpose, expirationTimeMillis);
    }

    private String generateValidTokenWithUserId(String userId, PurposeToken purpose) throws Exception {
        return generateValidTokenWithUserId(userId, purpose, System.currentTimeMillis() + 3600000);
    }

    private String generateValidTokenWithUserId(String userId, PurposeToken purpose, long expirationTimeMillis) throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("ctxh.com")
                .subject(userId)
                .audience("ctxh.com")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(new Date(expirationTimeMillis))
                .claim(AppConstants.PURPOSE, purpose.name())
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("test-key-id")
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        JWSSigner signer = new RSASSASigner(privateKey);
        jwsObject.sign(signer);

        return jwsObject.serialize();
    }
}
