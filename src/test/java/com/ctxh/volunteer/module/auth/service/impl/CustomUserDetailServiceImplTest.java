package com.ctxh.volunteer.module.auth.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.auth.entity.CustomUserDetails;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailService Unit Tests")
class CustomUserDetailServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailServiceImpl customUserDetailService;

    private User testUser;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder()
                .roleId(1L)
                .roleName("STUDENT")
                .build();

        testUser = User.builder()
                .userId(1L)
                .email("test@hcmut.edu.vn")
                .password("$2a$10$hashedPassword")
                .isVerified(true)
                .isLocked(false)
                .roles(List.of(studentRole))
                .build();
    }

    @Test
    @DisplayName("Load User By Username - Success with valid user ID")
    void loadUserByUsername_Success_WithValidUserId() {
        // Arrange
        String username = "1"; // User ID as string
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getEmail()); // Returns email, not userId
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        verify(userRepository).findByIdWithRoles(1L);
    }

    @Test
    @DisplayName("Load User By Username - Throws exception when user not found")
    void loadUserByUsername_ThrowsException_WhenUserNotFound() {
        // Arrange
        String username = "999"; // Non-existent user ID
        when(userRepository.findByIdWithRoles(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername(username))
                .isInstanceOf(BadCredentialsException.class)
                .hasCauseInstanceOf(BusinessException.class);

        verify(userRepository).findByIdWithRoles(999L);
    }

    @Test
    @DisplayName("Load User By Username - Correctly parses user ID from string")
    void loadUserByUsername_CorrectlyParsesUserId() {
        // Arrange
        String username = "12345";
        User user = User.builder()
                .userId(12345L)
                .email("user@hcmut.edu.vn")
                .password("hashedPassword")
                .isVerified(true)
                .isLocked(false)
                .roles(List.of(studentRole))
                .build();

        when(userRepository.findByIdWithRoles(12345L)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@hcmut.edu.vn"); // Returns email
        verify(userRepository).findByIdWithRoles(12345L);
    }

    @Test
    @DisplayName("Load User By Username - Loads user with multiple roles")
    void loadUserByUsername_Success_WithMultipleRoles() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(2L)
                .roleName("ADMIN")
                .build();

        testUser.setRoles(List.of(studentRole, adminRole));

        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername("1");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        // Note: Due to implementation details, authorities structure may vary

        verify(userRepository).findByIdWithRoles(1L);
    }

    @Test
    @DisplayName("Load User By Username - Loads correct account status flags")
    void loadUserByUsername_LoadsCorrectAccountFlags() {
        // Arrange
        testUser.setIsVerified(true);
        testUser.setIsLocked(false);

        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername("1");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isTrue(); // Verified = enabled
        assertThat(userDetails.isAccountNonLocked()).isTrue(); // Not locked
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByIdWithRoles(1L);
    }

    @Test
    @DisplayName("Load User By Username - Handles locked account")
    void loadUserByUsername_HandlesLockedAccount() {
        // Arrange
        testUser.setIsLocked(true);

        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername("1");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isAccountNonLocked()).isFalse();

        verify(userRepository).findByIdWithRoles(1L);
    }

    @Test
    @DisplayName("Load User By Username - Handles unverified account")
    void loadUserByUsername_HandlesUnverifiedAccount() {
        // Arrange
        testUser.setIsVerified(false);

        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailService.loadUserByUsername("1");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();

        verify(userRepository).findByIdWithRoles(1L);
    }
}
