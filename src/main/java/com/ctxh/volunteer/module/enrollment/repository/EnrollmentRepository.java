package com.ctxh.volunteer.module.enrollment.repository;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments by activity ID
     */
    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.student s " +
            "JOIN FETCH s.user " +
            "WHERE e.activity.activityId = :activityId " +
            "ORDER BY e.appliedAt DESC")
    List<Enrollment> findByActivityId(@Param("activityId") Long activityId);

    /**
     * Find enrollments by activity ID and status
     */
    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.student s " +
            "JOIN FETCH s.user " +
            "WHERE e.activity.activityId = :activityId " +
            "AND e.status = :status " +
            "ORDER BY e.appliedAt DESC")
    List<Enrollment> findByActivityIdAndStatus(
            @Param("activityId") Long activityId,
            @Param("status") EnrollmentStatus status
    );

    /**
     * Find enrollment by ID and activity ID (for authorization)
     */
    @Query("SELECT e FROM Enrollment e " +
            "WHERE e.enrollmentId = :enrollmentId " +
            "AND e.activity.activityId = :activityId")
    Optional<Enrollment> findByIdAndActivityId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("activityId") Long activityId
    );

    /**
     * Find enrollment by student ID and activity ID
     */
    @Query("SELECT e FROM Enrollment e " +
            "WHERE e.student.studentId = :studentId " +
            "AND e.activity.activityId = :activityId")
    Optional<Enrollment> findByStudentIdAndActivityId(
            @Param("studentId") Long studentId,
            @Param("activityId") Long activityId
    );

    /**
     * Find all enrollments by student ID
     */
    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.activity a " +
            "JOIN FETCH a.organization " +
            "WHERE e.student.studentId = :studentId " +
            "ORDER BY e.appliedAt DESC")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all enrollments by student ID and status
     */
    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.activity a " +
            "JOIN FETCH a.organization " +
            "WHERE e.student.studentId = :studentId " +
            "AND e.status = :status " +
            "ORDER BY e.appliedAt DESC")
    List<Enrollment> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status
    );

    /**
     * Find enrollment by ID and student ID (for authorization)
     */
    @Query("SELECT e FROM Enrollment e " +
            "WHERE e.enrollmentId = :enrollmentId " +
            "AND e.student.studentId = :studentId")
    Optional<Enrollment> findByIdAndStudentId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("studentId") Long studentId
    );

    Long countByActivityAndStatus(Activity activity, EnrollmentStatus status);

    /**
     * Count all enrollments by organization ID (through activity)
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.activity.organization.organizationId = :organizationId " +
            "AND e.activity.endDateTime < CURRENT_TIMESTAMP")
    Long countByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Count approved enrollments by organization ID (only for ended activities)
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.activity.organization.organizationId = :organizationId " +
            "AND (e.status = 'APPROVED' OR e.status = 'COMPLETED') " +
            "AND e.activity.endDateTime < CURRENT_TIMESTAMP ")
    Long countApprovedByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.activity.organization.organizationId = :organizationId " +
            "AND (e.status = 'APPROVED' OR e.status = 'COMPLETED')" +
            "AND e.activity.endDateTime < CURRENT_TIMESTAMP")
    Long countApprovedEndedByOrganizationId(@Param("organizationId") Long organizationId);
}
