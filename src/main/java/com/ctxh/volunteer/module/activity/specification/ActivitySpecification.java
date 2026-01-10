package com.ctxh.volunteer.module.activity.specification;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.enums.ActivityCategory;
import com.ctxh.volunteer.module.activity.enums.RegistrationState;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ActivitySpecification {

    /**
     * Advanced search specification
     */
    public static Specification<Activity> searchActivities(
            String keyword,
            ActivityCategory category,
            RegistrationState status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search in title, description, shortDescription
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likePattern
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likePattern
                );
                Predicate shortDescPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortDescription")),
                        likePattern
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate, shortDescPredicate));
            }

            // Category filter
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Status filter
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            } else {
                // Default: only show OPEN activities
                predicates.add(criteriaBuilder.equal(root.get("status"), RegistrationState.OPEN));
            }

            // Start date range filter
            if (startDate != null) {
                LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDateTime"), startDateTime));
            }

            // End date range filter
            if (endDate != null) {
                LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDateTime"), endDateTime));
            }

            // Order by start date ascending
            query.orderBy(criteriaBuilder.asc(root.get("startDateTime")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
