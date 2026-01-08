package com.ctxh.volunteer.module.student.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.attendance.repository.AttendanceRepository;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.certificate.dto.CertificateResponseDto;
import com.ctxh.volunteer.module.certificate.entity.Certificate;
import com.ctxh.volunteer.module.certificate.repository.CertificateRepository;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.student.dto.response.ParticipationHistoryDto;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import com.ctxh.volunteer.module.student.entity.Student;
import com.ctxh.volunteer.module.student.enums.Gender;
import com.ctxh.volunteer.module.student.repository.StudentRepository;
import com.ctxh.volunteer.module.student.service.StudentService;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.ctxh.volunteer.common.util.AppConstants.DEFAULT_AVATAR_URL;


@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final CertificateRepository certificateRepository;
    private final RoleRepository roleRepository;

    @Override
    public StudentResponseDto registerStudent(CreateStudentRequestDto requestDto) {
        // Validate MSSV uniqueness
        if (studentRepository.existsByMssv(requestDto.getMssv())) {
            throw new BusinessException(ErrorCode.MSSV_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        Role role = roleRepository.findByRoleName(RoleEnum.STUDENT.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .avatarUrl(DEFAULT_AVATAR_URL)
                .roles(List.of(role))
                .build();

        // Create student
        Student student = Student.builder()
                .user(user)
                .fullName(requestDto.getFullName())
                .mssv(requestDto.getMssv())
                .phoneNumber(requestDto.getPhoneNumber())
                .totalCtxhDays(0.0)
                .build();

        user.setStudent(student);
        userRepository.save(user);
        student.generateQrCode();
        Student savedStudent = studentRepository.save(student);
        log.info("Created student with ID: {}", savedStudent.getStudentId());

        return mapToStudentResponseDto(savedStudent);
    }


    // sau này có Authentication thì bỏ studentId vào lấy từ token
    @Transactional
    @Override
    public StudentResponseDto updateStudent(Long studentId, UpdateStudentRequestDto requestDto) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        Student student = user.getStudent();

        // Update fields if provided
        if (requestDto.getFullName() != null) {
            student.setFullName(requestDto.getFullName());
        }

        if (requestDto.getPhoneNumber() != null) {
            student.setPhoneNumber(requestDto.getPhoneNumber());
        }

        if (requestDto.getAcademicYear() != null) {
            student.setAcademicYear(requestDto.getAcademicYear());
        }
        if (requestDto.getFaculty() != null) {
            student.setFaculty(requestDto.getFaculty());
        }
        if (requestDto.getDateOfBirth() != null) {
            student.setDateOfBirth(requestDto.getDateOfBirth());
        }
        if (requestDto.getGender() != null) {
            student.setGender(Gender.valueOf(requestDto.getGender()));
        }

        if (requestDto.getBio() != null) {
            user.setBio(requestDto.getBio());
        }

        userRepository.save(user);
        log.info("Updated student with ID: {}", studentId);
        Student updatedStudent = studentRepository.save(student);
        return mapToStudentResponseDto(updatedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return mapToStudentResponseDto(student);
    }

    @Override
    public StudentResponseDto getStudentByMssv(String mssv) {
        Student student = studentRepository.findByMssv(mssv)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return mapToStudentResponseDto(student);
    }

    // ============ PARTICIPATION HISTORY ============

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationHistoryDto> getParticipationHistory(Long studentId) {
        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        return enrollments.stream()
                .map(enrollment -> {
                    // Find attendance for this enrollment
                    Optional<Attendance> attendance = attendanceRepository
                            .findByStudentIdAndActivityId(studentId, enrollment.getActivity().getActivityId());

                    // Find certificate for this enrollment
                    Optional<Certificate> certificate = certificateRepository
                            .findByEnrollment_EnrollmentId(enrollment.getEnrollmentId());

                    return ParticipationHistoryDto.builder()
                            // Enrollment info
                            .enrollmentId(enrollment.getEnrollmentId())
                            .enrollmentStatus(enrollment.getStatus())
                            .appliedAt(enrollment.getAppliedAt())
                            .approvedAt(enrollment.getApprovedAt())
                            .isCompleted(enrollment.getIsCompleted())
                            .completedAt(enrollment.getCompletedAt())
                            // Activity info
                            .activityId(enrollment.getActivity().getActivityId())
                            .activityTitle(enrollment.getActivity().getTitle())
                            .shortDescription(enrollment.getActivity().getShortDescription())
                            .category(enrollment.getActivity().getCategory())
                            .startDateTime(enrollment.getActivity().getStartDateTime())
                            .endDateTime(enrollment.getActivity().getEndDateTime())
                            .address(enrollment.getActivity().getAddress())
                            .ctxhHours(enrollment.getActivity().getTheNumberOfCtxhDay())
                            // Organization info
                            .organizationId(enrollment.getActivity().getOrganization().getOrganizationId())
                            .organizationName(enrollment.getActivity().getOrganization().getOrganizationName())
                            // Attendance info
                            .hasAttendance(attendance.isPresent())
                            .checkInTime(attendance.map(Attendance::getCheckInTime).orElse(null))
                            .checkOutTime(attendance.map(Attendance::getCheckOutTime).orElse(null))
                            .attendanceDuration(attendance.map(Attendance::getAttendanceDurationMinutes).orElse(null))
                            // Certificate info
                            .hasCertificate(certificate.isPresent())
                            .certificateCode(certificate.map(Certificate::getCertificateCode).orElse(null))
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDto> getStudentCertificates(Long studentId) {
        // Verify student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // Get all valid certificates for the student
        List<Certificate> certificates = certificateRepository.findValidCertificatesByStudentId(studentId);

        return certificates.stream()
                .map(certificate -> mapToCertificateResponseDto(certificate, student))
                .toList();
    }

    @Override
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToStudentResponseDto)
                .toList();
    }


    //    // ============ MAPPING METHODS ============
//
    private StudentResponseDto mapToStudentResponseDto(Student student) {
        return StudentResponseDto.builder()
                .studentId(student.getStudentId())
                .phoneNumber(student.getPhoneNumber())
                .email(student.getUser().getEmail())
                .fullName(student.getFullName())
                .mssv(student.getMssv())
                .academicYear(student.getAcademicYear())
                .faculty(student.getFaculty())
                .totalCtxhDays(student.getTotalCtxhDays())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .avatarUrl(student.getUser().getAvatarUrl())
                .bio(student.getUser().getBio())
                .qrCodeData(student.getQrCodeData())
                .createdAt(student.getCreateAt())
                .updatedAt(student.getUpdateAt())
                .build();
    }

    private CertificateResponseDto mapToCertificateResponseDto(Certificate certificate, Student student) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String activityPeriod = String.format("%s - %s",
                certificate.getActivityStartDate().format(dateTimeFormatter),
                certificate.getActivityEndDate().format(dateTimeFormatter)
        );

        return CertificateResponseDto.builder()
                // Certificate info
                .certificateId(certificate.getCertificateId())
                .certificateCode(certificate.getCertificateCode())
                .issuedDate(certificate.getIssuedDate())
                .isRevoked(certificate.getIsRevoked())
                // Student info (from certificate cache and student entity)
                .studentId(certificate.getStudentId())
                .studentName(certificate.getStudentName())
                .studentMssv(certificate.getStudentMssv())
                .studentFaculty(certificate.getStudentFaculty())
                .studentAcademicYear(certificate.getStudentAcademicYear())
                .studentDateOfBirth(student.getDateOfBirth())
                .studentGender(student.getGender() != null ? student.getGender().name() : null)
                // Activity info (from certificate cache)
                .activityId(certificate.getActivityId())
                .activityTitle(certificate.getActivityTitle())
                .activityStartDate(certificate.getActivityStartDate())
                .activityEndDate(certificate.getActivityEndDate())
                .ctxhHours(certificate.getCtxhHours())
                // Organization info (from certificate cache)
                .organizationName(certificate.getOrganizationName())
                .representativeName(certificate.getRepresentativeName())
                .representativeEmail(certificate.getRepresentativeEmail())
                // Enrollment info
                .enrollmentId(certificate.getEnrollment().getEnrollmentId())
                .completedAt(certificate.getEnrollment().getCompletedAt())
                // Formatted dates for display
                .issuedDateFormatted(certificate.getIssuedDate().format(dateFormatter))
                .activityPeriodFormatted(activityPeriod)
                .build();
    }
}
