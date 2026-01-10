package com.ctxh.volunteer.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_ARGUMENT(4000, "Invalid argument provided", HttpStatus.BAD_REQUEST),
    // ============ GENERAL ERRORS (1000-1099) ============
    INTERNAL_SERVER_ERROR(1000, "Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT(1001, "Invalid input provided", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1002, "Requested resource not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1003, "Authentication is required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1004, "You don't have permission to access this resource", HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(1005, "HTTP method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(1006, "Resource conflict occurred", HttpStatus.CONFLICT),
    BAD_REQUEST(1007, "Bad request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(1008, "Validation error", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1009, "User is not authenticated", HttpStatus.UNAUTHORIZED),

    // ============ AUTHENTICATION & AUTHORIZATION (1100-1199) ============
    INVALID_CREDENTIALS(1100, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1101, "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(1102, "Invalid token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(1103, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1104, "Account has been disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(1105, "Account has been locked", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(1106, "Email address is not verified", HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_TOKEN(1107, "Invalid verification token", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_EXPIRED(1108, "Password reset token has expired", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_RESET_TOKEN(1109, "Invalid password reset token", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSIONS(1110, "Insufficient permissions", HttpStatus.FORBIDDEN),
    TOKEN_GENERATION_FAILED(1111, "Failed to generate token", HttpStatus.INTERNAL_SERVER_ERROR),
    BUILD_OAUTH2_URL_FAILED(1112, "Failed to build OAuth2 URL", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_BANNED(1113, "Account has been banned", HttpStatus.FORBIDDEN),



    // ============ USER ERRORS (1200-1299) ============
    USER_NOT_FOUND(1200, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1201, "User with this email already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_REGISTERED(1202, "Email address is already registered", HttpStatus.CONFLICT),
    PHONE_ALREADY_REGISTERED(1203, "Phone number is already registered", HttpStatus.CONFLICT),
    INVALID_USER_TYPE(1204, "Invalid user type", HttpStatus.BAD_REQUEST),
    USER_CANNOT_BE_DELETED(1205, "User cannot be deleted", HttpStatus.BAD_REQUEST),
    EMAIL_OR_PASSWORD_INCORRECT(1206, "Email or password is incorrect", HttpStatus.BAD_REQUEST),
    NOT_EQUAL_PASSWORD(1207, "Password and confirm password do not match", HttpStatus.BAD_REQUEST),
    ERROR_RESET_PASSWORD(1208, "Error occurred while resetting password", HttpStatus.INTERNAL_SERVER_ERROR),

    // ============ STUDENT ERRORS (1300-1399) ============
    STUDENT_NOT_FOUND(1300, "Student not found", HttpStatus.NOT_FOUND),
    MSSV_ALREADY_EXISTS(1301, "Student ID (MSSV) already exists", HttpStatus.CONFLICT),
    INVALID_MSSV_FORMAT(1302, "Invalid student ID (MSSV) format", HttpStatus.BAD_REQUEST),
    STUDENT_NOT_ELIGIBLE(1303, "Student is not eligible for this action", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_CTXH_HOURS(1304, "Insufficient community service hours", HttpStatus.BAD_REQUEST),

    // ============ ORGANIZATION ERRORS (1400-1499) ============
    ORGANIZATION_NOT_FOUND(1400, "Organization not found", HttpStatus.NOT_FOUND),
    ORGANIZATION_NOT_VERIFIED(1401, "Organization is not verified", HttpStatus.FORBIDDEN),
    ORGANIZATION_NAME_ALREADY_EXISTS(1402, "Organization name already exists", HttpStatus.CONFLICT),
    INVALID_ORGANIZATION_TYPE(1403, "Invalid organization type", HttpStatus.BAD_REQUEST),
    ORGANIZATION_SUSPENDED(1404, "Organization has been suspended", HttpStatus.FORBIDDEN),
    INVALID_RATING_FOR_ORGANIZATION(1405, "Rating must be between 1 and 5", HttpStatus.BAD_REQUEST),

    // ============ ACTIVITY ERRORS (1500-1599) ============
    ACTIVITY_NOT_FOUND(1500, "Activity not found", HttpStatus.NOT_FOUND),
    ACTIVITY_ALREADY_CLOSED(1501, "Activity registration is closed", HttpStatus.BAD_REQUEST),
    ACTIVITY_FULL(1502, "Activity has reached maximum participants", HttpStatus.BAD_REQUEST),
    ACTIVITY_NOT_OPEN(1503, "Activity is not open for registration", HttpStatus.BAD_REQUEST),
    ACTIVITY_ALREADY_STARTED(1504, "Activity has already started", HttpStatus.BAD_REQUEST),
    ACTIVITY_CANNOT_BE_MODIFIED(1505, "Activity cannot be modified", HttpStatus.BAD_REQUEST),
    REGISTRATION_DEADLINE_PASSED(1506, "Registration deadline has passed", HttpStatus.BAD_REQUEST),
    INVALID_ACTIVITY_DATE(1507, "Invalid activity date range", HttpStatus.BAD_REQUEST),
    ACTIVITY_ALREADY_COMPLETED(1508, "Activity has already been completed", HttpStatus.BAD_REQUEST),
    NOT_ACTIVITY_OWNER(1509, "You are not the owner of this activity", HttpStatus.FORBIDDEN),
    ACTIVITY_CANNOT_REGISTER(1510, "Cannot register for this activity", HttpStatus.BAD_REQUEST),
    INVALID_REGISTRATION_DEADLINE(1511, "Invalid registration deadline", HttpStatus.BAD_REQUEST),
    ACTIVITY_NOT_OPEN_FOR_ENROLLMENT(1512, "Activity is not open for enrollment", HttpStatus.BAD_REQUEST),
    ACTIVITY_REGISTRATION_DEADLINE_PASSED(1513, "Activity registration deadline has passed", HttpStatus.BAD_REQUEST),
    ACTIVITY_MAX_PENDING_REACHED(1514, "Activity has reached maximum pending enrollments", HttpStatus.BAD_REQUEST),

    // ============ ENROLLMENT ERRORS (1600-1699) ============
    ENROLLMENT_NOT_FOUND(1600, "Enrollment not found", HttpStatus.NOT_FOUND),
    ALREADY_ENROLLED(1601, "You have already enrolled in this activity", HttpStatus.CONFLICT),
    ENROLLMENT_PENDING(1602, "Your enrollment is pending approval", HttpStatus.BAD_REQUEST),
    ENROLLMENT_REJECTED(1603, "Your enrollment has been rejected", HttpStatus.BAD_REQUEST),
    ENROLLMENT_CANNOT_BE_CANCELLED(1604, "Enrollment cannot be cancelled", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_APPROVED(1605, "Enrollment is not approved", HttpStatus.BAD_REQUEST),
    ENROLLMENT_ALREADY_COMPLETED(1606, "Enrollment has already been completed", HttpStatus.BAD_REQUEST),
    INVALID_ENROLLMENT_STATUS(1607, "Invalid enrollment status", HttpStatus.BAD_REQUEST),
    ENROLLMENT_ALREADY_APPROVED(1608, "Enrollment has already been approved", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_PENDING(1609, "Enrollment is not pending", HttpStatus.BAD_REQUEST),

    // ============ ATTENDANCE ERRORS (1700-1799) ============
    ATTENDANCE_NOT_FOUND(1700, "Attendance record not found", HttpStatus.NOT_FOUND),
    ALREADY_CHECKED_IN(1701, "Student has already checked in", HttpStatus.CONFLICT),
    ALREADY_CHECKED_OUT(1702, "Student has already checked out", HttpStatus.CONFLICT),
    NOT_CHECKED_IN(1703, "Student has not checked in yet", HttpStatus.BAD_REQUEST),
    INVALID_QR_CODE(1704, "Invalid QR code", HttpStatus.BAD_REQUEST),
    QR_CODE_EXPIRED(1705, "QR code has expired", HttpStatus.BAD_REQUEST),
    ATTENDANCE_NOT_ALLOWED(1706, "Attendance is not allowed at this time", HttpStatus.BAD_REQUEST),
    STUDENT_NOT_ENROLLED(1707, "Student is not enrolled in this activity", HttpStatus.BAD_REQUEST),

    // ============ CERTIFICATE ERRORS (1800-1899) ============
    CERTIFICATE_NOT_FOUND(1800, "Certificate not found", HttpStatus.NOT_FOUND),
    CERTIFICATE_ALREADY_ISSUED(1801, "Certificate has already been issued", HttpStatus.CONFLICT),
    CERTIFICATE_REVOKED(1802, "Certificate has been revoked", HttpStatus.BAD_REQUEST),
    CERTIFICATE_EXPIRED(1803, "Certificate has expired", HttpStatus.BAD_REQUEST),
    NOT_ELIGIBLE_FOR_CERTIFICATE(1804, "Not eligible for certificate", HttpStatus.BAD_REQUEST),
    INVALID_CERTIFICATE_CODE(1805, "Invalid certificate code", HttpStatus.BAD_REQUEST),

    // ============ TASK ERRORS (1900-1999) ============
    TASK_NOT_FOUND(1900, "Task not found", HttpStatus.NOT_FOUND),
    TASK_ALREADY_COMPLETED(1901, "Task has already been completed", HttpStatus.BAD_REQUEST),
    TASK_CANNOT_BE_MODIFIED(1902, "Task cannot be modified", HttpStatus.BAD_REQUEST),
    INVALID_TASK_STATUS(1903, "Invalid task status", HttpStatus.BAD_REQUEST),
    TASK_OVERDUE(1904, "Task is overdue", HttpStatus.BAD_REQUEST),

    // ============ FILE ERRORS (2000-2099) ============
    FILE_UPLOAD_FAILED(2000, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE(2001, "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(2002, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(2003, "File not found", HttpStatus.NOT_FOUND),
    FILE_DOWNLOAD_FAILED(2004, "File download failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ============ EXTERNAL SERVICE ERRORS (2100-2199) ============
    EMAIL_SERVICE_ERROR(2100, "Email service error", HttpStatus.INTERNAL_SERVER_ERROR),
    SMS_SERVICE_ERROR(2101, "SMS service error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_SERVICE_ERROR(2102, "Payment service error", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_API_ERROR(2103, "External API error", HttpStatus.BAD_GATEWAY),
    SERVICE_UNAVAILABLE(2104, "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),


    // ============ ROLE ERRORS (2200-2299) ============
    ROLE_NOT_FOUND(2200, "Role not found", HttpStatus.NOT_FOUND),

    // ============ EMAIL VERIFICATION ERRORS (2300-2399) ============
    VERIFY_EMAIL_FAILED(2300, "Email verification failed", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ALREADY_VERIFIED(2301, "User email is already verified", HttpStatus.BAD_REQUEST),

    // ============ MAIL SENDING ERRORS (2400-2499) ============
    MAIL_SENDING_FAILED(2400, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_DOMAIN_NOT_ALLOWED(2401, "Email must be a valid HCMUT email address", HttpStatus.BAD_REQUEST),
    EMAIL_HAS_BEEN_REGISTERED(2402, "Email has already been registered", HttpStatus.CONFLICT),
    EMAIL_ORGANIZATION_NOT_ALLOWED(2403, "Invalid email format for Organization", HttpStatus.BAD_REQUEST),

    FAILED_TO_UPLOAD_IMAGE(2500, "Failed to upload image", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
