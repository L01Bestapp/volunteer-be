package com.ctxh.volunteer.module.activity.entity;

import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.task.entity.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ctxh.volunteer.common.util.AppConstants.REGISTRATION_MULTIPLIER;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activity_org", columnList = "organization_id"),
//        @Index(name = "idx_activity_start_date", columnList = "start_date"),
//        @Index(name = "idx_activity_status", columnList = "status"),
        @Index(name = "idx_activity_category", columnList = "category"),
//        @Index(name = "idx_activity_registration_deadline", columnList = "registration_deadline"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity extends BaseEntity {

    @Id
    @Tsid
    private Long activityId;

    // ============ RELATIONSHIP WITH ORGANIZATION ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activity_organization"))
    @JsonIgnore
    private Organization organization;

    // ============ BASIC INFORMATION ============
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private ActivityCategory category;
    // ============ DATE & TIME ============

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "registration_opens_at")
    @Builder.Default
    private LocalDateTime registrationOpensAt = LocalDateTime.now();

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    // ============ LOCATION ============
    @Column(name = "address", length = 500)
    private String address;
    // ============ PARTICIPANTS ============

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Column(name = "pending_participants", nullable = false)
    @Builder.Default
    private Integer pendingParticipants = 0;

    @Column(name = "approved_participants", nullable = false)
    @Builder.Default
    private Integer approvedParticipants = 0;

    @Column(name = "actual_participants")
    private Integer actualParticipants; // Số người thực sự tham gia

    // ============ STATUS ============
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ActivityStatus status = ActivityStatus.OPEN;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============ REQUIREMENTS ============
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // ============ BENEFITS & REWARDS ============
    @Column(name = "the_number_of_ctxh_day", nullable = false)
    private Double theNumberOfCtxhDay;

    // ============ RELATIONSHIPS ============

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Attendance> attendances = new ArrayList<>();
    // ============ HELPER METHODS ============

    /**
     * Check if a new registration is allowed (for PENDING)
     */
    public boolean canRegister() {
        // Nếu activity không mở đăng ký thì thôi
        if (status != ActivityStatus.OPEN) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_OPEN_FOR_ENROLLMENT);
        }

        // Hết hạn đăng ký
        if (isRegistrationDeadlinePassed()) {
            throw new BusinessException(ErrorCode.ACTIVITY_REGISTRATION_DEADLINE_PASSED);
        }

        // ĐÃ ĐỦ NGƯỜI ĐƯỢC DUYỆT -> KHÔNG CHO ĐĂNG KÝ THÊM
        if (approvedParticipants >= maxParticipants) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        // Chưa đến ngày mở đăng ký
        if (registrationOpensAt != null && LocalDateTime.now().isBefore(registrationOpensAt)) {
            return false;
        }

        // Giới hạn tổng số đăng ký (PENDING + APPROVED)
        int maxRegistrations = maxParticipants * REGISTRATION_MULTIPLIER;

        return currentParticipants < maxRegistrations;
    }

    /**
     * Check if has available slots
     */
    public boolean hasAvailableSlots() {
        return maxParticipants == null || approvedParticipants < maxParticipants;
    }

    /**
     * Get remaining slots
     */
    public Integer getRemainingSlots() {
        if (maxParticipants == null) {
            return null; // Unlimited
        }
        return Math.max(0, maxParticipants - approvedParticipants);
    }

    /**
     * Increment participants
     */
    public void incrementParticipants() {
        this.approvedParticipants++;
        this.currentParticipants++;

        if (maxParticipants != null && approvedParticipants >= maxParticipants) {
            this.status = ActivityStatus.FULL;
        }
    }

    /**
     * Increment pending count
     */
    public void incrementPending() {
        if (!canRegister()) {
            throw new BusinessException(ErrorCode.ACTIVITY_CANNOT_REGISTER);
        }
        this.pendingParticipants++;
        this.currentParticipants++;
    }

    /**
     * Decrement pending count
     */
    public void decrementPending() {
        if (this.pendingParticipants > 0) {
            this.pendingParticipants--;
            this.currentParticipants--;
        }
    }

    /**
     * Move from pending to approve
     */
    public void approvePending() {
        if (this.pendingParticipants <= 0) {
            return;
        }

        if (maxParticipants != null && approvedParticipants >= maxParticipants) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        this.pendingParticipants--;
        incrementParticipants();
    }

    /**
     * Open registration
     */
    public void openRegistration() {
        this.status = ActivityStatus.OPEN;
    }

    /**
     * Close registration
     */
    public void closeRegistration() {
        this.status = ActivityStatus.CLOSED;
    }

    /**
     * Complete activity
     */
    public void complete() {
        this.status = ActivityStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Check if activity has started
     */
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startDateTime);
    }

    /**
     * Check if activity has ended
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endDateTime);
    }

    /**
     * Check if the registration deadline passed
     */
    public boolean isRegistrationDeadlinePassed() {
        return registrationDeadline != null && LocalDateTime.now().isAfter(registrationDeadline);
    }

    /**
     * Add enrollment
     */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setActivity(this);
    }

    /**
     * Remove enrollment
     */
    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setActivity(null);
    }

    /**
     * Add a task
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setActivity(this);
    }

    /**
     * Remove task
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setActivity(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;
        Activity activity = (Activity) o;
        return activityId != null && activityId.equals(activity.activityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}