package com.ctxh.volunteer.module.organization.repository;

import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByOrganizationName(String organizationName);

    List<Organization> findAllByVerificationStatus(VerificationStatus verificationStatus);
}
