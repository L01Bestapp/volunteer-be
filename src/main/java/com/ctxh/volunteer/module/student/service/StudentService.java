package com.ctxh.volunteer.module.student.service;

import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.StudentListResponseDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    StudentResponseDto registerStudent(CreateStudentRequestDto requestDto);
    //  --------------------------------------------------------------

//    /**
//     * Get student by ID
//     */
//    StudentResponseDto getStudentById(Long studentId);
//
//    /**
//     * Get student by MSSV
//     */
//    StudentResponseDto getStudentByMssv(String mssv);
//
//    /**
//     * Create new student
//     */
//    StudentResponseDto createStudent(CreateStudentRequestDto requestDto);
//
//    /**
//     * Update student information
//     */
//    StudentResponseDto updateStudent(Long studentId, UpdateStudentRequestDto requestDto);
//
//    /**
//     * Delete student
//     */
//    void deleteStudent(Long studentId);
//
//    /**
//     * Get all students with pagination
//     */
//    Page<StudentListResponseDto> getAllStudents(Pageable pageable);
//
//    /**
//     * Search students by keyword
//     */
//    Page<StudentListResponseDto> searchStudents(String keyword, Pageable pageable);
//
//    /**
//     * Get students by faculty
//     */
//    Page<StudentListResponseDto> getStudentsByFaculty(String faculty, Pageable pageable);
//
//    /**
//     * Get students by academic year
//     */
//    Page<StudentListResponseDto> getStudentsByAcademicYear(String academicYear, Pageable pageable);
//
//    /**
//     * Generate QR code for student
//     */
//    StudentResponseDto generateQrCode(Long studentId);
//
//    /**
//     * Get student by QR code
//     */
//    StudentResponseDto getStudentByQrCode(String qrCodeData);
//
//    /**
//     * Update CTXH days
//     */
//    StudentResponseDto updateCtxhDays(Long studentId, Double days);
}
