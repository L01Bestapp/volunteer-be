package com.ctxh.volunteer.organization;

import com.ctxh.volunteer.activity.entity.Activity;
import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_org_name", columnNames = "organization_name")
        },
        indexes = {
                @Index(name = "idx_org_user", columnList = "user_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Id
    @Tsid
    private Long organizationId;

    // ============ ONE-TO-ONE WITH USER ============

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id",foreignKey = @ForeignKey(name = "fk_organization_user"))
    @JsonIgnore
    private User user;

    // ============ ORGANIZATION INFORMATION ============
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "organization_name", nullable = false, unique = true, length = 200)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private OrganizationType type;

    @Column(name = "short_name", length = 50)
    private String shortName; // Tên viết tắt

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "mission", columnDefinition = "TEXT")
    private String mission; // Sứ mệnh

    @Column(name = "vision", columnDefinition = "TEXT")
    private String vision; // Tầm nhìn

    // ============ SOCIAL MEDIA ============
    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "youtube_url", length = 255)
    private String youtubeUrl;

    // ============ REPRESENTATIVE INFORMATION ============
    @Column(name = "representative_name", length = 100)
    private String representativeName;

    @Column(name = "representative_phone", length = 20)
    private String representativePhone;

    @Column(name = "representative_email", length = 100)
    private String representativeEmail;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;
    // ============ STATISTICS ============

    @Column(name = "total_activities_created", nullable = false)
    @Builder.Default
    private Integer totalActivitiesCreated = 0;

    private Integer completedActivitiesCount = 0;

    // ============ RATING ============

    @Column(name = "average_rating", precision = 3)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "rating_sum", nullable = false)
    @Builder.Default
    private Integer ratingSum = 0;

    // ============ RELATIONSHIPS ============

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();

    // ============ ENUMS ============

    /**
     * Organization Type enum
     */
    public enum OrganizationType {
        UNIVERSITY_DEPARTMENT,  // Phòng ban trường
        STUDENT_UNION,          // Đoàn - Hội sinh viên
        CLUB,                   // Câu lạc bộ
        NGO,                    // Tổ chức phi chính phủ
        COMPANY,                // Doanh nghiệp
        GOVERNMENT,             // Cơ quan chính phủ
        CHARITY,                // Từ thiện
        FOUNDATION,             // Quỹ
        COMMUNITY_GROUP,        // Nhóm cộng đồng
        OTHER                   // Khác
    }

    /**
     * Verification Status enum
     */
    public enum VerificationStatus {
        PENDING,        // Chờ xét duyệt
        APPROVED,       // Đã duyệt
        REJECTED,       // Bị từ chối
        NEED_MORE_INFO  // Cần bổ sung thông tin
    }

    // ============ HELPER METHODS ============

    /**
     * Add activity
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.setOrganization(this);
        this.totalActivitiesCreated++;
    }

    /**
     * Remove activity
     */
    public void removeActivity(Activity activity) {
        activities.remove(activity);
        activity.setOrganization(null);
        this.totalActivitiesCreated--;
    }

    /**
     * Mark activity as completed
     */
    public void markActivityCompleted() {
        this.completedActivitiesCount++;
    }

    /**
     * Add rating
     */
    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.INVALID_RATING_FOR_ORGANIZATION);
        }
        this.ratingSum += rating;
        this.totalRatings++;
        this.averageRating = (double) this.ratingSum / this.totalRatings;
    }



    /**
     * Get completion rate
     */
    public double getCompletionRate() {
        if (totalActivitiesCreated == 0) {
            return 0.0;
        }
        return (completedActivitiesCount * 100.0) / totalActivitiesCreated;
    }

    /**
     * Check if profile is complete
     */
    public boolean isProfileComplete() {
        return organizationName != null && !organizationName.isEmpty()
                && type != null
                && description != null && !description.isEmpty()
                && representativeName != null && !representativeName.isEmpty();
    }

    /**
     * Check if can create activities
     */
    public boolean canCreateActivities() {
        return isVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization)) return false;
        Organization that = (Organization) o;
        return organizationId != null && organizationId.equals(that.organizationId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Organization{" +
                "organizationId=" + organizationId +
                ", organizationName='" + organizationName + '\'' +
                ", type=" + type +
                ", isVerified=" + isVerified +
                ", totalActivitiesCreated=" + totalActivitiesCreated +
                '}';
    }
}