package com.ctxh.volunteer.module.student.dto.request;

import com.ctxh.volunteer.module.student.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateStudentRequestDto {
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?84[0-9]{9}$", message = "Phone number must be a valid Vietnam phone number")
    private String phoneNumber;

    @Pattern(regexp = "^[0-9]{7}$", message = "MSSV must be exactly 7 digits")
    private String mssv;

    @Size(max = 20, message = "Academic year must not exceed 20 characters")
    private String academicYear;

    @Size(max = 100, message = "Faculty must not exceed 100 characters")
    private String faculty;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    private String bio;
}
