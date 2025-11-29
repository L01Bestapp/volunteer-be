package com.ctxh.volunteer.module.student.repository;

import com.ctxh.volunteer.module.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by MSSV
     */
    Optional<Student> findByMssv(String mssv);

    /**
     * Find student by user ID
     */
    Optional<Student> findByUser_UserId(Long userId);

    /**
     * Find student by QR code data
     */
    Optional<Student> findByQrCodeData(String qrCodeData);

    /**
     * Check if MSSV exists
     */
    boolean existsByMssv(String mssv);

    /**
     * Check if user already has a student profile
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * Find students by faculty
     */
    Page<Student> findByFacultyContainingIgnoreCase(String faculty, Pageable pageable);

    /**
     * Find students by academic year
     */
    Page<Student> findByAcademicYear(String academicYear, Pageable pageable);

    /**
     * Search students by name or MSSV
     */
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.mssv LIKE CONCAT('%', :keyword, '%')")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find students with minimum CTXH days
     */
    @Query("SELECT s FROM Student s WHERE s.totalCtxhDays >= :minDays")
    Page<Student> findStudentsWithMinCtxhDays(@Param("minDays") Double minDays, Pageable pageable);

    /**
     * Find students with incomplete profiles
     */
    @Query("SELECT s FROM Student s WHERE " +
            "s.fullName IS NULL OR s.fullName = '' OR " +
            "s.mssv IS NULL OR s.mssv = '' OR " +
            "s.dateOfBirth IS NULL OR " +
            "s.gender IS NULL")
    Page<Student> findStudentsWithIncompleteProfile(Pageable pageable);
}
