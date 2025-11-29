package com.ctxh.volunteer.module.auth.entity;

import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @Tsid
    private Long userId;

    @Column(name = "email",unique = true, nullable = false,length = 100)
    private String email;

    @JsonIgnore
    private String password;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    // ============ RELATIONSHIPS ============

    @ManyToMany
    private List<Role> roles;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expires_at")
    private LocalDateTime resetPasswordTokenExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_password_change_at")
    private LocalDateTime lastPasswordChangeAt;

    // Associations to Student and Organization entities
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Organization organization;

    // ============ BUSINESS HELPER METHODS ============

    /**
     * Check if user is a student
     */
    public boolean isStudent() {
        return roles.stream().anyMatch(role -> role.getRoleName().equals("ROLE_STUDENT"))
                && student != null;
    }

    /**
     * Check if user is an organization
     */
    public boolean isOrganization() {
        return roles.stream().anyMatch(role -> role.getRoleName().equals("ROLE_ORGANIZATION"))
                && organization != null;
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return roles.stream().anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN"));
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.isLocked = false;
        this.lockedUntil = null;
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;

        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.isLocked = true;
            this.lockedUntil = LocalDateTime.now().plusHours(1); // Lock for 1 hour
        }
    }

    /**
     * Generate verification token
     */
    public void generateVerificationToken(String token) {
        this.verificationToken = token;
        this.verificationTokenExpiresAt = LocalDateTime.now().plusHours(24); // Valid for 24 hours
    }

    /**
     * Verify email
     */
    public void verifyEmail() {
        this.isVerified = true;
        this.verificationToken = null;
        this.verificationTokenExpiresAt = null;
    }

    /**
     * Check if verification token is valid
     */
    public boolean isVerificationTokenValid() {
        return verificationToken != null
                && verificationTokenExpiresAt != null
                && LocalDateTime.now().isBefore(verificationTokenExpiresAt);
    }

    /**
     * Generate password reset token
     */
    public void generatePasswordResetToken(String token) {
        this.resetPasswordToken = token;
        this.resetPasswordTokenExpiresAt = LocalDateTime.now().plusHours(1); // Valid for 1 hour
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid() {
        return resetPasswordToken != null
                && resetPasswordTokenExpiresAt != null
                && LocalDateTime.now().isBefore(resetPasswordTokenExpiresAt);
    }

    /**
     * Reset password
     */
    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.resetPasswordToken = null;
        this.resetPasswordTokenExpiresAt = null;
        this.lastPasswordChangeAt = LocalDateTime.now();
    }

    /**
     * Change password
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.lastPasswordChangeAt = LocalDateTime.now();
    }

    /**
     * Lock account manually
     */
    public void lockAccount() {
        this.isLocked = true;
        this.lockedUntil = null; // Permanent lock until manually unlocked
    }

    /**
     * Unlock account
     */
    public void unlockAccount() {
        this.isLocked = false;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
