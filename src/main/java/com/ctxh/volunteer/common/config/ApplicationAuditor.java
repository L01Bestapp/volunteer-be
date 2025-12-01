package com.ctxh.volunteer.common.config;

import com.ctxh.volunteer.module.auth.entity.CustomUserDetails;
import com.ctxh.volunteer.module.auth.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class ApplicationAuditor implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()  ||
                authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        if (customUserDetails == null) {
            return Optional.empty();
        }
        User user = customUserDetails.getUser();
        if (user != null) {
            return Optional.of(user.getUserId());
        }
        return Optional.empty();
    }
}
