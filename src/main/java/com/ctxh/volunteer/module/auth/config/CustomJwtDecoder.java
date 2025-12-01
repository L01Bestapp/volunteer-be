package com.ctxh.volunteer.module.auth.config;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.auth.enums.PurposeToken;
import com.ctxh.volunteer.module.auth.service.IntrospectToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final IntrospectToken introspectToken;
    private final NimbusJwtDecoder nimbusJwtDecoder;
    private final RSAKeyRecord rsaKeyRecord;

    @Override
    public Jwt decode(String token) {
        boolean isValid = introspectToken.verifyToken(token, rsaKeyRecord.rsaPublicKey(), PurposeToken.ACCESS);
        if (!isValid) {
            throw new InvalidBearerTokenException("Invalid JWT token", new BusinessException(ErrorCode.TOKEN_INVALID));
        }
        return nimbusJwtDecoder.decode(token);
    }

}
