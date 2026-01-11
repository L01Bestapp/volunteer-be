package com.ctxh.volunteer.module.notification.repository;

import com.ctxh.volunteer.module.notification.entity.Notification;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user
     */
    List<Notification> findByUserUserId(Long userId);

    /**
     * Find all unread notifications for a user
     */
    List<Notification> findByUserUserIdAndIsReadFalse(Long userId);

    /**
     * Count unread notifications for a user
     */
    Long countByUserUserIdAndIsReadFalse(Long userId);

    /**
     * Find notifications by user and type
     */
    List<Notification> findByUserUserIdAndType(Long userId, NotificationType type);

    /**
     * Check if a reminder notification already exists for a user and activity
     * Using native query to access JSONB field
     */
    @Query(value = "SELECT * FROM notifications n WHERE n.user_id = :userId " +
            "AND n.type = :type " +
            "AND n.data->>'activityId' = :activityId",
            nativeQuery = true)
    Optional<Notification> findByUserIdAndTypeAndActivityId(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("activityId") String activityId
    );

    /**
     * Delete all notifications for a user
     */
    void deleteByUserUserId(Long userId);
}
