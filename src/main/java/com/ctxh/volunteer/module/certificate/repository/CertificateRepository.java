package com.ctxh.volunteer.module.certificate.repository;

import com.ctxh.volunteer.module.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /**
     * Find certificate by code
     */
    Optional<Certificate> findByCertificateCode(String certificateCode);

    /**
     * Find certificate by enrollment ID
     */
    Optional<Certificate> findByEnrollment_EnrollmentId(Long enrollmentId);

    /**
     * Find all certificates by student ID
     */
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.studentId = :studentId " +
            "ORDER BY c.issuedDate DESC")
    List<Certificate> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all valid (not revoked) certificates by student ID
     */
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.studentId = :studentId " +
            "AND c.isRevoked = false " +
            "ORDER BY c.issuedDate DESC")
    List<Certificate> findValidCertificatesByStudentId(@Param("studentId") Long studentId);

    /**
     * Check if certificate exists for enrollment
     */
    boolean existsByEnrollment_EnrollmentId(Long enrollmentId);
}
