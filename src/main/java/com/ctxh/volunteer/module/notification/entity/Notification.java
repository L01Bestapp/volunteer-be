package com.ctxh.volunteer.module.notification.entity;

import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    // ============ RELATIONSHIPS ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_user"))
    private User user;

    // ============ NOTIFICATION CONTENT ============

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    // ============ DATA ============
    // Lưu thông tin bổ sung dưới dạng JSON (ví dụ: activityId, enrollmentId, etc.)
    @Type(JsonType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    private Map<String, Object> data;

    // ============ STATUS ============

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_sent", nullable = false)
    @Builder.Default
    private Boolean isSent = false;

    // ============ HELPER METHODS ============

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Mark notification as unread
     */
    public void markAsUnread() {
        this.isRead = false;
    }

    /**
     * Mark notification as sent
     */
    public void markAsSent() {
        this.isSent = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return notificationId != null && notificationId.equals(that.notificationId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
