package com.ctxh.volunteer.module.auth.service.impl;

import com.ctxh.volunteer.common.util.AppConstants;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.ctxh.volunteer.module.auth.service.IntrospectToken;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Slf4j
@Service
public class IntrospectTokenImpl implements IntrospectToken {
    @Override
    public boolean verifyToken(String token, RSAPublicKey publicKey, PurposeToken expectedPurpose) {
        try {
            JWTClaimsSet claimsSet = parseAndVerifyToken(token, publicKey);
            // purpose access
            if (!expectedPurpose.name().equals(claimsSet.getClaim(AppConstants.PURPOSE))) return false;

            if (claimsSet.getExpirationTime().before(new Date())) {
                log.warn("Token has expired");
                return false;
            }
            // Additional checks can be added here, such as audience, issuer, etc.
            return true;
        } catch (Exception e) {
            log.error("Token verification failed", e);
        }
        return false;
    }

    @Override
    public JWTClaimsSet parseAndVerifyToken(String token, RSAPublicKey publicKey) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jwsObject.verify(verifier)) {
                log.warn("Invalid token signature");
                return null;
            }
            return JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
        } catch (Exception e) {
            log.info("get ClaimSet token failed", e);
        }
        return null;
    }
}
