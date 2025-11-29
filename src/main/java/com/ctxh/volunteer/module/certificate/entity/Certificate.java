package com.ctxh.volunteer.module.certificate.entity;

import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_certificate_enrollment", columnNames = "enrollment_id"),
                @UniqueConstraint(name = "uk_certificate_code", columnNames = "certificate_code")
        },
        indexes = {
                @Index(name = "idx_certificate_code", columnList = "certificate_code"),
                @Index(name = "idx_certificate_student", columnList = "student_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate extends BaseEntity {

    @Id
    @Tsid
    private Long certificateId;

    // ============ RELATIONSHIP ============

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_certificate_enrollment"))
    private Enrollment enrollment;

    // ============ CERTIFICATE INFO ============

    @Column(name = "certificate_code", nullable = false, unique = true, length = 100)
    private String certificateCode;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    // ============ CACHED STUDENT INFO (for certificate generation) ============

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    @Column(name = "student_mssv", length = 20)
    private String studentMssv;

    @Column(name = "student_faculty", length = 100)
    private String studentFaculty;

    @Column(name = "student_academic_year", length = 20)
    private String studentAcademicYear;

    // ============ CACHED ACTIVITY INFO (for certificate generation) ============

    @Column(name = "activity_title", nullable = false, length = 200)
    private String activityTitle;

    @Column(name = "activity_start_date")
    private LocalDateTime activityStartDate;

    @Column(name = "activity_end_date")
    private LocalDateTime activityEndDate;

    @Column(name = "ctxh_hours", nullable = false)
    private Double ctxhHours;

    // ============ CACHED ORGANIZATION INFO (for certificate generation) ============

    @Column(name = "organization_name", nullable = false, length = 200)
    private String organizationName;

    @Column(name = "organization_address", length = 500)
    private String organizationAddress;

    @Column(name = "organization_contact", length = 100)
    private String organizationContact;

    // ============ STATUS ============

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 500)
    private String revokeReason;

    // ============ HELPER METHODS ============

    /**
     * Generate unique certificate code
     */
    public static String generateCertificateCode() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Issue certificate
     */
    public void issue() {
        this.issuedDate = LocalDateTime.now();
        if (this.certificateCode == null || this.certificateCode.isEmpty()) {
            this.certificateCode = generateCertificateCode();
        }
    }

    /**
     * Revoke certificate
     */
    public void revoke(String reason) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokeReason = reason;
    }

    /**
     * Check if certificate is valid (not revoked)
     */
    public boolean isValid() {
        return !this.isRevoked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Certificate)) return false;
        Certificate that = (Certificate) o;
        return certificateId != null && certificateId.equals(that.certificateId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
