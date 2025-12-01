package com.ctxh.volunteer.module.auth.config;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserDetailsService userDetailsService;

    @Autowired
    public CustomAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String userId = jwt.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        if (!userDetails.isAccountNonLocked()) throw new LockedException("User locked", new BusinessException(ErrorCode.ACCOUNT_LOCKED));
        if (!userDetails.isEnabled()) throw new DisabledException("User disabled", new BusinessException(ErrorCode.ACCOUNT_DISABLED));

        log.info("authorities: {}", userDetails.getAuthorities());
        return new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
    }
}
