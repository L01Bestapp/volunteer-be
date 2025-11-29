package com.ctxh.volunteer.module.auth.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private transient User user;

    // implementation of UserDetails interface methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .toList().toString()
        );
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonLocked() {
        if (Boolean.TRUE.equals(user.getIsLocked()) && user.getLockedUntil() != null) {
            // Auto-unlock if lock period has passed
            if (LocalDateTime.now().isAfter(user.getLockedUntil())) {
                user.setIsLocked(false);
                user.setLockedUntil(null);
                user.setFailedLoginAttempts(0);
                return true;
            }
            return false;
        }
        return !user.getIsLocked();
    }


    @Override
    public boolean isEnabled() {
        return user.getIsVerified();
    }
}
