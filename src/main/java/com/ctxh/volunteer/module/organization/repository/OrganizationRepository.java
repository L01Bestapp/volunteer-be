package com.ctxh.volunteer.module.organization.repository;

import com.ctxh.volunteer.module.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByOrganizationName(String organizationName);
}
