package com.ctxh.volunteer.module.activity.repository;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    /**
     * Find all activities by organization ID
     */
    @Query("SELECT a FROM Activity a WHERE a.organization.organizationId = :organizationId ORDER BY a.createAt DESC")
    List<Activity> findByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Find activity by ID and organization ID (for authorization)
     */
    @Query("SELECT a FROM Activity a WHERE a.activityId = :activityId AND a.organization.organizationId = :organizationId")
    Optional<Activity> findByIdAndOrganizationId(
            @Param("activityId") Long activityId,
            @Param("organizationId") Long organizationId
    );

    /**
     * Find all activities by status
     */
    List<Activity> findByStatusOrderByCreateAtDesc(ActivityStatus status);

    /**
     * Find all available activities (OPEN status and registration deadline not passed)
     */
    @Query("SELECT a FROM Activity a WHERE a.status = 'OPEN' " +
            "AND (a.registrationDeadline IS NULL OR a.registrationDeadline > :now) " +
            "ORDER BY a.startDateTime ASC")
    List<Activity> findAvailableActivities(@Param("now") LocalDateTime now);

    /**
     * Simple search by keyword in title or description
     */
    @Query("SELECT a FROM Activity a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND a.status = 'OPEN' " +
            "ORDER BY a.startDateTime ASC")
    List<Activity> searchByKeyword(@Param("keyword") String keyword);
}
