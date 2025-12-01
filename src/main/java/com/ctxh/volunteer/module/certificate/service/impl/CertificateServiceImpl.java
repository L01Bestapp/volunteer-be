package com.ctxh.volunteer.module.certificate.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.certificate.repository.CertificateRepository;
import com.ctxh.volunteer.module.certificate.service.CertificateService;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;

    @Override
    @Transactional
    public Certificate generateCertificate(Enrollment enrollment) {
        // Check if the certificate already exists
        if (certificateRepository.existsByEnrollment_EnrollmentId(enrollment.getEnrollmentId())) {
            throw new BusinessException(ErrorCode.CERTIFICATE_ALREADY_ISSUED);
        }

        // Verify enrollment is completed
        if (Boolean.FALSE.equals(enrollment.getIsCompleted())) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_APPROVED);
        }

        Student student = enrollment.getStudent();
        Activity activity = enrollment.getActivity();
        Organization organization = activity.getOrganization();

        // Build certificate with cached data - USE HELPER METHOD
        Certificate certificate = Certificate.builder()
                .enrollment(enrollment)
                // Student IDs
                .studentId(student.getStudentId())
                .activityId(activity.getActivityId())
                // Cache student info
                .studentName(student.getFullName())
                .studentMssv(student.getMssv())
                .studentFaculty(student.getFaculty())
                .studentAcademicYear(student.getAcademicYear())
                // Cache activity info
                .activityTitle(activity.getTitle())
                .activityStartDate(activity.getStartDateTime())
                .activityEndDate(activity.getEndDateTime())
                .ctxhHours(activity.getTheNumberOfCtxhDay())
                // Cache organization info
                .organizationName(organization.getOrganizationName())
                .representativeName(organization.getRepresentativeName())
                .representativeEmail(organization.getRepresentativeEmail())
                .build();

        // Issue certificate - USE HELPER METHOD
        certificate.issue(); // Sets issuedDate and generates certificateCode

        Certificate savedCertificate = certificateRepository.save(certificate);
        log.info("Generated certificate {} for enrollment {}",
                savedCertificate.getCertificateCode(),
                enrollment.getEnrollmentId());

        return savedCertificate;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean certificateExists(Long enrollmentId) {
        return certificateRepository.existsByEnrollment_EnrollmentId(enrollmentId);
    }
}
