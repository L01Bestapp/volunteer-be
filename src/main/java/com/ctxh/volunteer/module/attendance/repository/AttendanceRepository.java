package com.ctxh.volunteer.module.attendance.repository;

import com.ctxh.volunteer.module.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Find all attendance records by activity ID
     */
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.student s " +
            "JOIN FETCH s.user " +
            "WHERE a.activity.activityId = :activityId " +
            "ORDER BY a.checkInTime DESC")
    List<Attendance> findByActivityId(@Param("activityId") Long activityId);

    /**
     * Find attendance by student ID and activity ID
     */
    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.studentId = :studentId " +
            "AND a.activity.activityId = :activityId")
    Optional<Attendance> findByStudentIdAndActivityId(
            @Param("studentId") Long studentId,
            @Param("activityId") Long activityId
    );

    /**
     * Find all attendance records by student ID
     */
    @Query("SELECT a FROM Attendance a " +
            "JOIN FETCH a.activity act " +
            "JOIN FETCH act.organization " +
            "WHERE a.student.studentId = :studentId " +
            "ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Count present students for an activity
     */
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.activity.activityId = :activityId " +
            "AND a.status = 'PRESENT'")
    Long countPresentByActivityId(@Param("activityId") Long activityId);

    /**
     * Count absent students for an activity
     */
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.activity.activityId = :activityId " +
            "AND a.status = 'ABSENT'")
    Long countAbsentByActivityId(@Param("activityId") Long activityId);

    /**
     * Count students who checked in
     */
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.activity.activityId = :activityId " +
            "AND a.checkInTime IS NOT NULL")
    Long countCheckedInByActivityId(@Param("activityId") Long activityId);

    /**
     * Count students who checked out
     */
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.activity.activityId = :activityId " +
            "AND a.checkOutTime IS NOT NULL")
    Long countCheckedOutByActivityId(@Param("activityId") Long activityId);

    /**
     * Find attendance by student ID, activity ID and date
     */
    @Query("SELECT a FROM Attendance a " +
            "WHERE a.student.studentId = :studentId " +
            "AND a.activity.activityId = :activityId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Optional<Attendance> findByStudentIdAndActivityIdAndDate(
            @Param("studentId") Long studentId,
            @Param("activityId") Long activityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
