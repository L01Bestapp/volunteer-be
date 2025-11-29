package com.ctxh.volunteer.module.certificate.service;

import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;

public interface CertificateService {

    /**
     * Generate certificate for completed enrollment
     */
    Certificate generateCertificate(Enrollment enrollment);

    /**
     * Check if certificate exists for enrollment
     */
    boolean certificateExists(Long enrollmentId);
}
