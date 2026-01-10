package com.ctxh.volunteer.module.student.service;

import com.ctxh.volunteer.module.certificate.dto.CertificateResponseDto;
import com.ctxh.volunteer.module.student.dto.response.ParticipationHistoryDto;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface StudentService {
    StudentResponseDto registerStudent(CreateStudentRequestDto requestDto);

    StudentResponseDto updateStudent(Long studentId, @Valid UpdateStudentRequestDto requestDto);

    StudentResponseDto getStudentById(Long studentId);

    StudentResponseDto getStudentByMssv(String mssv);

    // ============ PARTICIPATION HISTORY ============

    /**
     * Get student's participation history
     */
    List<ParticipationHistoryDto> getParticipationHistory(Long studentId);

    /**
     * Get student's certificates
     */
    List<CertificateResponseDto> getStudentCertificates(Long studentId);

    List<StudentResponseDto> getAllStudents();

    StudentResponseDto getMyQrCode();
    //  --------------------------------------------------------------

}
