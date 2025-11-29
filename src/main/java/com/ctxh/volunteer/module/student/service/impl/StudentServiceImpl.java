package com.ctxh.volunteer.module.student.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
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

import static com.ctxh.volunteer.common.util.AppConstants.DEFAULT_AVATAR_URL;


@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public StudentResponseDto registerStudent(CreateStudentRequestDto requestDto) {
        // Validate MSSV uniqueness
        if (studentRepository.existsByMssv(requestDto.getMssv())) {
            throw new BusinessException(ErrorCode.MSSV_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .avatarUrl(DEFAULT_AVATAR_URL)
                .build();

        // Create student
        Student student = Student.builder()
                .user(user)
                .fullName(requestDto.getFullName())
                .mssv(requestDto.getMssv())
                .gender(Gender.valueOf(requestDto.getGender()))
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


    //    // ============ STUDENT CRUD OPERATIONS ============

//    @Override
//    @Transactional
//    public void deleteStudent(Long studentId) {
//        if (!studentRepository.existsById(studentId)) {
//            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
//        }
//        studentRepository.deleteById(studentId);
//        log.info("Deleted student with ID: {}", studentId);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<StudentListResponseDto> getAllStudents(Pageable pageable) {
//        return studentRepository.findAll(pageable)
//                .map(this::mapToStudentListResponseDto);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<StudentListResponseDto> searchStudents(String keyword, Pageable pageable) {
//        return studentRepository.searchStudents(keyword, pageable)
//                .map(this::mapToStudentListResponseDto);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<StudentListResponseDto> getStudentsByFaculty(String faculty, Pageable pageable) {
//        return studentRepository.findByFacultyContainingIgnoreCase(faculty, pageable)
//                .map(this::mapToStudentListResponseDto);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<StudentListResponseDto> getStudentsByAcademicYear(String academicYear, Pageable pageable) {
//        return studentRepository.findByAcademicYear(academicYear, pageable)
//                .map(this::mapToStudentListResponseDto);
//    }
//
//    @Override
//    @Transactional
//    public StudentResponseDto generateQrCode(Long studentId) {
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
//
//        student.generateQrCode();
//        Student updatedStudent = studentRepository.save(student);
//        log.info("Generated QR code for student with ID: {}", studentId);
//
//        return mapToStudentResponseDto(updatedStudent);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public StudentResponseDto getStudentByQrCode(String qrCodeData) {
//        Student student = studentRepository.findByQrCodeData(qrCodeData)
//                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_QR_CODE));
//
//        // Check if QR code is expired
//        if (student.isQrCodeExpired()) {
//            throw new BusinessException(ErrorCode.QR_CODE_EXPIRED);
//        }
//
//        return mapToStudentResponseDto(student);
//    }
//
//    @Override
//    @Transactional
//    public StudentResponseDto updateCtxhDays(Long studentId, Double days) {
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
//
//        student.setTotalCtxhDays(student.getTotalCtxhDays() + days);
//        Student updatedStudent = studentRepository.save(student);
//        log.info("Updated CTXH days for student with ID: {}. New total: {}",
//                studentId, updatedStudent.getTotalCtxhDays());
//
//        return mapToStudentResponseDto(updatedStudent);
//    }
//
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
//
//    private StudentListResponseDto mapToStudentListResponseDto(Student student) {
//        return StudentListResponseDto.builder()
//                .studentId(student.getStudentId())
//                .fullName(student.getFullName())
//                .mssv(student.getMssv())
//                .faculty(student.getFaculty())
//                .totalCtxhDays(student.getTotalCtxhDays())
//                .gender(student.getGender())
//                .avatarUrl(student.getAvatarUrl())
//                .build();
//    }
}
