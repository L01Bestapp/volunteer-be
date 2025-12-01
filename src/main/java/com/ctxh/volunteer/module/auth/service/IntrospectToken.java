package com.ctxh.volunteer.module.auth.service;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.interfaces.RSAPublicKey;

public interface IntrospectToken {
    boolean verifyToken(String token, RSAPublicKey publicKey, PurposeToken expectedPurpose);
    JWTClaimsSet parseAndVerifyToken(String token, RSAPublicKey publicKey);
}
