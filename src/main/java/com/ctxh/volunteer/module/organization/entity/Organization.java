package com.ctxh.volunteer.module.organization.entity;

import com.ctxh.volunteer.common.entity.BaseEntity;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import com.ctxh.volunteer.module.organization.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Long organizationId;

    // ============ ONE-TO-ONE WITH USER ============

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id",foreignKey = @ForeignKey(name = "fk_organization_user"))
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();

    // ============ ORGANIZATION INFORMATION ============
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "organization_name", nullable = false, unique = true, length = 200)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private OrganizationType type;

    // ============ REPRESENTATIVE INFORMATION ============
    @Column(name = "representative_name", length = 100)
    private String representativeName;

    @Column(name = "representative_phone", length = 20)
    private String representativePhone;

    @Column(name = "representative_email", length = 100)
    private String representativeEmail;
    // ============ STATISTICS ============

    @Column(name = "total_activities_created", nullable = false)
    @Builder.Default
    private Integer totalActivitiesCreated = 0;

    private Integer completedActivitiesCount = 0;

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
     * Get completion rate
     */
    public double getCompletionRate() {
        if (totalActivitiesCreated == 0) {
            return 0.0;
        }
        return (completedActivitiesCount * 100.0) / totalActivitiesCreated;
    }

    /**
     * Check if can create activities
     */
    public boolean canCreateActivities() {
        return verificationStatus == VerificationStatus.APPROVED;
    }

    public void activeOrganization() {
        this.verificationStatus = VerificationStatus.APPROVED;
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
                ", totalActivitiesCreated=" + totalActivitiesCreated +
                '}';
    }
}