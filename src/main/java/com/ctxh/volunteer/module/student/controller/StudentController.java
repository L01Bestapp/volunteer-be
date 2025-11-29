package com.ctxh.volunteer.module.student.controller;

import com.ctxh.volunteer.common.dto.ApiResponse;
import com.ctxh.volunteer.module.student.dto.request.CreateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.request.UpdateStudentRequestDto;
import com.ctxh.volunteer.module.student.dto.response.StudentListResponseDto;
import com.ctxh.volunteer.module.student.dto.response.StudentResponseDto;
import com.ctxh.volunteer.module.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    /**
     * Create new student
     * POST /api/v1/students
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentResponseDto> createStudent(
            @Valid @RequestBody CreateStudentRequestDto requestDto) {
        return ApiResponse.ok(
                "Student created successfully",
                studentService.registerStudent(requestDto)
        );
    }

//    // --------------------------------------------------------------
//
//    /**
//     * Get student by ID
//     * GET /api/v1/students/{id}
//     */
//    @GetMapping("/{id}")
//    public ApiResponse<StudentResponseDto> getStudentById(@PathVariable("id") Long studentId) {
//        return ApiResponse.ok(studentService.getStudentById(studentId));
//    }
//
//    /**
//     * Get student by MSSV
//     * GET /api/v1/students/mssv/{mssv}
//     */
//    @GetMapping("/mssv/{mssv}")
//    public ApiResponse<StudentResponseDto> getStudentByMssv(@PathVariable("mssv") String mssv) {
//        return ApiResponse.ok(
//                "Student retrieved successfully",
//                studentService.getStudentByMssv(mssv)
//        );
//    }
//

//
//    /**
//     * Update student information
//     * PUT /api/v1/students/{id}
//     */
//    @PutMapping("/{id}")
//    public ApiResponse<StudentResponseDto> updateStudent(
//            @PathVariable("id") Long studentId,
//            @Valid @RequestBody UpdateStudentRequestDto requestDto) {
//        return ApiResponse.ok(
//                "Student updated successfully",
//                studentService.updateStudent(studentId, requestDto)
//        );
//    }
//
//    /**
//     * Delete student
//     * DELETE /api/v1/students/{id}
//     */
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public ApiResponse<Void> deleteStudent(@PathVariable("id") Long studentId) {
//        studentService.deleteStudent(studentId);
//        return ApiResponse.ok("Student deleted successfully", null);
//    }
//
//    /**
//     * Get all students with pagination
//     * GET /api/v1/students
//     */
//    @GetMapping
//    public ApiResponse<Page<StudentListResponseDto>> getAllStudents(
//            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
//            Pageable pageable) {
//        return ApiResponse.ok(studentService.getAllStudents(pageable)
//        );
//    }
//
//    /**
//     * Search students by keyword (name or MSSV)
//     * GET /api/v1/students/search?keyword={keyword}
//     */
//    @GetMapping("/search")
//    public ApiResponse<Page<StudentListResponseDto>> searchStudents(
//            @RequestParam("keyword") String keyword,
//            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC)
//            Pageable pageable) {
//        return ApiResponse.ok(
//                "Search completed successfully",
//                studentService.searchStudents(keyword, pageable)
//        );
//    }
//
//    /**
//     * Get students by faculty
//     * GET /api/v1/students/faculty?name={facultyName}
//     */
//    @GetMapping("/faculty")
//    public ApiResponse<Page<StudentListResponseDto>> getStudentsByFaculty(
//            @RequestParam("name") String facultyName,
//            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC)
//            Pageable pageable) {
//        return ApiResponse.ok(
//                "Students retrieved successfully",
//                studentService.getStudentsByFaculty(facultyName, pageable)
//        );
//    }
//
//    /**
//     * Get students by academic year
//     * GET /api/v1/students/academic-year?year={year}
//     */
//    @GetMapping("/academic-year")
//    public ApiResponse<Page<StudentListResponseDto>> getStudentsByAcademicYear(
//            @RequestParam("year") String year,
//            @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC)
//            Pageable pageable) {
//        return ApiResponse.ok(
//                "Students retrieved successfully",
//                studentService.getStudentsByAcademicYear(year, pageable)
//        );
//    }
//
//    /**
//     * Generate QR code for student
//     * POST /api/v1/students/{id}/qr-code
//     */
//    @PostMapping("/{id}/qr-code")
//    public ApiResponse<StudentResponseDto> generateQrCode(@PathVariable("id") Long studentId) {
//        return ApiResponse.ok(
//                "QR code generated successfully",
//                studentService.generateQrCode(studentId)
//        );
//    }
//
//    /**
//     * Get student by QR code
//     * GET /api/v1/students/qr-code?data={qrCodeData}
//     */
//    @GetMapping("/qr-code")
//    public ApiResponse<StudentResponseDto> getStudentByQrCode(
//            @RequestParam("data") String qrCodeData) {
//        return ApiResponse.ok(
//                "Student retrieved successfully",
//                studentService.getStudentByQrCode(qrCodeData)
//        );
//    }
//
//    /**
//     * Update CTXH days for student
//     * PATCH /api/v1/students/{id}/ctxh-days?days={days}
//     */
//    @PutMapping("/{id}/ctxh-days")
//    public ApiResponse<StudentResponseDto> updateCtxhDays(
//            @PathVariable("id") Long studentId,
//            @RequestParam("days") Double days) {
//        return ApiResponse.ok(
//                "CTXH days updated successfully",
//                studentService.updateCtxhDays(studentId, days)
//        );
//    }
}
