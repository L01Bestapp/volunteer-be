package com.ctxh.volunteer.module.student.dto.request;

import com.ctxh.volunteer.common.util.EnumValidation;
import com.ctxh.volunteer.module.student.enums.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStudentRequestDto {
    // User information
    @Pattern(regexp = "^\\+?(84|0)\\d{9}$", message = "Phone number must be a valid Vietnam phone number")
    private String phoneNumber;

    // Student information
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @EnumValidation(name = "Gender", enumClass = Gender.class)
    private String gender;

    @Size(max = 20, message = "Academic year must not exceed 20 characters")
    private String academicYear;

    @Size(max = 100, message = "Faculty must not exceed 100 characters")
    private String faculty;

    private LocalDate dateOfBirth;

    private String bio;
}
