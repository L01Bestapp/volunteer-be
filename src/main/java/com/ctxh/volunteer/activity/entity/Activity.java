package com.ctxh.volunteer.activity.entity;

import com.ctxh.volunteer.attendance.entity.Attendance;
import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.enrollment.entity.Enrollment;
import com.ctxh.volunteer.organization.Organization;
import com.ctxh.volunteer.tag.Tag;
import com.ctxh.volunteer.task.entity.Task;
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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activity_org", columnList = "organization_id"),
//        @Index(name = "idx_activity_start_date", columnList = "start_date"),
//        @Index(name = "idx_activity_status", columnList = "status"),
//        @Index(name = "idx_activity_category", columnList = "category"),
//        @Index(name = "idx_activity_registration_deadline", columnList = "registration_deadline"),
//        @Index(name = "idx_activity_featured", columnList = "is_featured")
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
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private ActivityCategory category;

    @ManyToMany
    private List<Tag> tags;

    // ============ DATE & TIME ============

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "registration_opens_at")
    private LocalDateTime registrationOpensAt;

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
    private ActivityStatus status = ActivityStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ============ REQUIREMENTS ============
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // ============ BENEFITS & REWARDS ============

    private Integer benefitsCtxh;

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

    // ============ ENUMS ============

    /**
     * Activity Status enum
     */
    public enum ActivityStatus {
        DRAFT,          // Nháp
        PUBLISHED,      // Đã xuất bản
        OPEN,           // Đang tuyển
        FULL,           // Đã đủ người
        CLOSED,         // Đã đóng đăng ký
        IN_PROGRESS,    // Đang diễn ra
        COMPLETED,      // Đã hoàn thành
        CANCELLED       // Đã hủy
    }

    /**
     * Activity Category enum
     */
    public enum ActivityCategory {
        EDUCATION,          // Giáo dục
        HEALTHCARE,         // Y tế
        ENVIRONMENT,        // Môi trường
        COMMUNITY,          // Cộng đồng
        CULTURE,            // Văn hóa
        SPORTS,             // Thể thao
        TECHNOLOGY,         // Công nghệ
        SOCIAL_WELFARE,     // Phúc lợi xã hội
        DISASTER_RELIEF,    // Cứu trợ thiên tai
        ANIMAL_WELFARE,     // Phúc lợi động vật
        ELDERLY_CARE,       // Chăm sóc người cao tuổi
        CHILDREN,           // Trẻ em
        DISABILITY_SUPPORT, // Hỗ trợ người khuyết tật
        POVERTY_ALLEVIATION,// Xóa đói giảm nghèo
        OTHER               // Khác
    }


    // ============ HELPER METHODS ============

    /**
     * Check if registration is open
     */
    public boolean isRegistrationOpen() {

        if (status != ActivityStatus.PUBLISHED && status != ActivityStatus.OPEN) {
            return false;
        }

        if (registrationDeadline != null && LocalDateTime.now().isAfter(registrationDeadline)) {
            return false;
        }

        if (maxParticipants != null && approvedParticipants >= maxParticipants) {
            return false;
        }

        return !(registrationOpensAt != null && LocalDateTime.now().isBefore(registrationOpensAt));
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
     * Decrement participants
     */
    public void decrementParticipants() {
        if (this.approvedParticipants > 0) {
            this.approvedParticipants--;
            this.currentParticipants--;
        }

        if (this.status == ActivityStatus.FULL && hasAvailableSlots()) {
            this.status = ActivityStatus.OPEN;
        }
    }

    /**
     * Increment pending count
     */
    public void incrementPending() {
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
     * Move from pending to approved
     */
    public void approvePending() {
        if (this.pendingParticipants > 0) {
            this.pendingParticipants--;
            incrementParticipants();
        }
    }

    /**
     * Publish activity
     */
    public void publish() {
        this.status = ActivityStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
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
     * Cancel activity
     */
    public void cancel(String reason) {
        this.status = ActivityStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
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
     * Check if registration deadline passed
     */
    public boolean isRegistrationDeadlinePassed() {
        return registrationDeadline != null &&
                LocalDateTime.now().isAfter(registrationDeadline);
    }

    /**
     * Get duration in days
     */
    public int getDurationInDays() {
        return (int) ChronoUnit.DAYS.between(startDateTime, endDateTime) + 1;
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
     * Add task
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