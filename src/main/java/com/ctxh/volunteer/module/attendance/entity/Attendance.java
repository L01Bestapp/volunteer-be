package com.ctxh.volunteer.module.attendance.entity;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.attendance.enums.AttendanceStatus;
import com.ctxh.volunteer.module.student.entity.Student;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_student_activity_date",
                        columnNames = {"student_id", "activity_id", "attendance_date"})
        },
        indexes = {
                @Index(name = "idx_attendance_student", columnList = "student_id"),
                @Index(name = "idx_attendance_activity", columnList = "activity_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    // ============ RELATIONSHIPS ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_activity"))
    private Activity activity;

    // ============ DATE & TIME ============
    @Column(name = "attendance_date", nullable = false)
    private LocalDateTime attendanceDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // ============ STATUS ============
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    // ============ ENUMS ============


    // ============ HELPER METHODS ============

    /**
     * Check-in student
     */
    public void checkIn() {
        if (this.checkInTime != null) {
            throw new IllegalStateException("Student has already checked in");
        }
        this.checkInTime = LocalDateTime.now();
        this.status = AttendanceStatus.PRESENT;
    }

    /**
     * Check-out student
     */
    public void checkOut() {
        if (this.checkInTime == null) {
            throw new IllegalStateException("Student must check in before checking out");
        }
        if (this.checkOutTime != null) {
            throw new IllegalStateException("Student has already checked out");
        }
        this.checkOutTime = LocalDateTime.now();
    }

    /**
     * Check if student has checked in
     */
    public boolean hasCheckedIn() {
        return this.checkInTime != null;
    }

    /**
     * Check if student has checked out
     */
    public boolean hasCheckedOut() {
        return this.checkOutTime != null;
    }

    /**
     * Check if attendance is complete (both check-in and check-out)
     */
    public boolean isComplete() {
        return this.checkInTime != null && this.checkOutTime != null;
    }

    /**
     * Mark as present (for manual marking)
     */
    public void markAsPresent() {
        this.status = AttendanceStatus.PRESENT;
    }

    /**
     * Mark as absent
     */
    public void markAsAbsent() {
        this.status = AttendanceStatus.ABSENT;
    }

    /**
     * Get attendance duration in minutes
     */
    public Long getAttendanceDurationMinutes() {
        if (checkInTime == null || checkOutTime == null) {
            return null;
        }
        return Duration.between(checkInTime, checkOutTime).toMinutes();
    }

    /**
     * Check if student is present (checked in)
     */
    public boolean isPresent() {
        return this.status == AttendanceStatus.PRESENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attendance)) return false;
        Attendance that = (Attendance) o;
        return attendanceId != null && attendanceId.equals(that.attendanceId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}