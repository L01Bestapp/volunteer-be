package com.ctxh.volunteer.enrollment.entity;

import com.ctxh.volunteer.activity.entity.Activity;
import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.student.entity.Student;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enrollment_student_activity",
                        columnNames = {"student_id", "activity_id"})
        },
        indexes = {
                @Index(name = "idx_enrollment_student", columnList = "student_id"),
                @Index(name = "idx_enrollment_activity", columnList = "activity_id"),
                @Index(name = "idx_enrollment_status", columnList = "status"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    @Id
    @Tsid
    private Long enrollmentId;

    // ============ RELATIONSHIPS ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_enrollment_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_enrollment_activity"))
    private Activity activity;

    // ============ ENROLLMENT INFO ============

    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    // ============ TIMESTAMPS ============

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy; // User ID who approved

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy; // User ID who rejected

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============ COMPLETION ============

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    /**
     * Enrollment Status enum
     */
    public enum EnrollmentStatus {
        PENDING,    // Chờ duyệt
        APPROVED,   // Đã duyệt
        REJECTED,   // Bị từ chối
    }

    // ============ HELPER METHODS ============

    /**
     * Approve enrollment
     */
    public void approve(Long approvedByUserId) {
        this.status = EnrollmentStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedByUserId;

        // Update activity participant count
        if (activity != null) {
            if (this.status == EnrollmentStatus.PENDING) {
                activity.approvePending();
            } else {
                activity.incrementParticipants();
            }
        }
    }

    /**
     * Reject enrollment
     */
    public void reject(Long rejectedByUserId) {
        this.status = EnrollmentStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedBy = rejectedByUserId;

        // Update activity counts
        if (activity != null && this.status == EnrollmentStatus.PENDING) {
            activity.decrementPending();
        }
    }

    /**
     * Mark as completed
     */
    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();

        // Update student's total CTXH hours
        if (student != null && activity.getBenefitsCtxh() != null) {
            student.updateCtxhDays(activity.getBenefitsCtxh());
        }
    }

    /**
     * Check if can be cancelled
     */
    public boolean canBeCancelled() {
        if (activity == null) {
            return false;
        }

        // Can't cancel if activity has started
        if (activity.hasStarted()) {
            return false;
        }

        return status == EnrollmentStatus.APPROVED ||
                status == EnrollmentStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment that = (Enrollment) o;
        return enrollmentId != null && enrollmentId.equals(that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}