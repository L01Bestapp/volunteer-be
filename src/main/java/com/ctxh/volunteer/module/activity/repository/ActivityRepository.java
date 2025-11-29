package com.ctxh.volunteer.module.activity.repository;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

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
}
