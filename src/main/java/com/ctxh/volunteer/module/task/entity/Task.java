package com.ctxh.volunteer.module.task.entity;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.common.entity.BaseEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_activity", columnList = "activity_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Id
    @Tsid
    private Long taskId;

    // ============ RELATIONSHIP ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_activity"))
    private Activity activity;

    // ============ BASIC INFO ============

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_type", length = 50)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    /**
     * Task Type enum
     */
    public enum TaskType {
        PREPARATION,    // Chuẩn bị
        MAIN_ACTIVITY,  // Hoạt động chính
        CLEANUP,        // Dọn dẹp
        LOGISTICS,      // Hậu cần
        COORDINATION,   // Điều phối
        DOCUMENTATION,  // Ghi chép
        MEDIA,          // Truyền thông
        REGISTRATION,   // Đăng ký
        FEEDBACK,       // Thu thập phản hồi
        OTHER           // Khác
    }

    // ============ HELPER METHODS ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return taskId != null && taskId.equals(task.taskId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}



