package com.ctxh.volunteer.module.student.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.certificate.dto.CertificateResponseDto;
import com.ctxh.volunteer.module.student.dto.ParticipationHistoryDto;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import com.ctxh.volunteer.module.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    /**
     * Create a new student
     * POST /api/v1/students/register
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentResponseDto> createStudent(
            @Valid @RequestBody CreateStudentRequestDto requestDto) {
        return ApiResponse.ok(
                "Student created successfully",
                studentService.registerStudent(requestDto)
        );
    }

    /**
     * Update student information
     * PUT /api/v1/students/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<StudentResponseDto> updateStudent(
            @PathVariable("id") Long studentId,
            @Valid @RequestBody UpdateStudentRequestDto requestDto) {
        return ApiResponse.ok(
                "Student updated successfully",
                studentService.updateStudent(studentId, requestDto)
        );
    }


    // sau này cần sửa getStudentById có param lấy từ Authenicated user

    /**
     * Get student by ID
     * GET /api/v1/students/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<StudentResponseDto> getStudentById(@PathVariable("id") Long studentId) {
        return ApiResponse.ok(studentService.getStudentById(studentId));
    }

    /**
     * Get student by MSSV
     * GET /api/v1/students/mssv/{mssv}
     */
    @GetMapping("/mssv/{mssv}")
    public ApiResponse<StudentResponseDto> getStudentByMssv(@PathVariable("mssv") String mssv) {
        return ApiResponse.ok(
                "Student retrieved successfully",
                studentService.getStudentByMssv(mssv)
        );
    }

    // ============ PARTICIPATION HISTORY ============

    /**
     * Get student's participation history
     * GET /api/v1/students/participation/history
     */
    @Operation(summary = "get student's participation history")
    @GetMapping("/participation/history")
    public ApiResponse<List<ParticipationHistoryDto>> getParticipationHistory(
            @RequestParam("studentId") Long studentId) {
        return ApiResponse.ok(
                "Participation history retrieved successfully",
                studentService.getParticipationHistory(studentId)
        );
    }

    /**
     * Get student's certificates
     * GET /api/v1/students/certificates
     */
    @Operation(summary = "get student's certificates")
    @GetMapping("/certificates")
    public ApiResponse<List<CertificateResponseDto>> getStudentCertificates(
            @RequestParam("studentId") Long studentId) {
        return ApiResponse.ok(
                "Certificates retrieved successfully",
                studentService.getStudentCertificates(studentId)
        );
    }

}