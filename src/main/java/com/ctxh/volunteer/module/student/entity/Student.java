package com.ctxh.volunteer.module.student.entity;

import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_student_mssv", columnNames = "mssv")
        },
        indexes = {
                @Index(name = "idx_student_mssv", columnList = "mssv"),
                @Index(name = "idx_student_user", columnList = "user_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {
    @Id
    @Tsid
    private Long studentId;

    // ============ RELATIONSHIP ============
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_student_user"))
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Attendance> attendances = new ArrayList<>();

    // ============ STUDENT INFORMATION ============

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "mssv", unique = true, length = 7)
    private String mssv;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "faculty", length = 100)
    private String faculty;

    // ============ CTXH TRACKING ============

    @Column(name = "total_ctxh_days", nullable = false)
    @Builder.Default
    private Double totalCtxhDays = 0.0;

    // ============ PERSONAL INFORMATION ============

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    // ============ PROFILE ============
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    // ============ QR CODE ============

    @Column(name = "qr_code_data", length = 500, unique = true)
    private String qrCodeData;

    @Column(name = "qr_code_generated_at")
    private LocalDateTime qrCodeGeneratedAt;

    // ============ HELPER METHODS ============

    /**
     * Add enrollment
     */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setStudent(this);
    }

    /**
     * Remove enrollment
     */
    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setStudent(null);
    }

    /**
     * Add attendance
     */
    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setStudent(this);
    }

    /**
     * Update CTXH hours
     */
    public void updateCtxhDays(int days) {
        if (days > 0) {
            this.totalCtxhDays += days;
        }
    }

    /**
     * Get CTXH completion percentage
     */
    public double getCtxhCompletionPercentage() {
        if (totalCtxhDays == 0) {
            return 0.0;
        }
        return Math.min(100.0, (totalCtxhDays * 100.0) / 15);
    }

    /**
     * Generate QR code data
     */
    public void generateQrCode() {
        this.qrCodeData = generateQrCodeString();
        this.qrCodeGeneratedAt = LocalDateTime.now();
    }

    /**
     * Generate unique QR code string
     */
    private String generateQrCodeString() {
        return String.format("STUDENT-%d-%s-%d",
                studentId,
                mssv != null ? mssv : "UNKNOWN",
                System.currentTimeMillis());
    }

    /**
     * Check if QR code needs regeneration (older than 30 days)
     */
    public boolean isQrCodeExpired() {
        if (qrCodeGeneratedAt == null) {
            return true;
        }
        return qrCodeGeneratedAt.plusDays(30)
                .isBefore(java.time.LocalDateTime.now());
    }

    /**
     * Get age
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return fullName != null && !fullName.isEmpty()
                && mssv != null && !mssv.isEmpty()
                && dateOfBirth != null
                && gender != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return studentId != null && studentId.equals(student.studentId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
