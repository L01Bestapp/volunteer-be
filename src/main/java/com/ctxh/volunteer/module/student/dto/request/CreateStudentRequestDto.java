package com.ctxh.volunteer.module.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStudentRequestDto {
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@hcmut\\.edu\\.vn$", message = "email must be a valid HCMUT email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "^([\\p{L}]{2,})(\\s+[\\p{L}]{2,}){1,6}$",
    message = "Full name must contain only letters and spaces, with at least two words")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "MSSV is required")
    @Pattern(regexp = "^\\d{7}$", message = "MSSV must be exactly 7 digits")
    private String mssv;

//    @EnumValidation(name = "Gender", enumClass = Gender.class)
//    private String gender;
    @Pattern(regexp = "^\\+?(84|0)\\d{9}$", message = "Phone number must be a valid Vietnam phone number")
    private String phoneNumber;
}
