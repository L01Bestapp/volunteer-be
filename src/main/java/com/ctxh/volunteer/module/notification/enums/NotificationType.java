package com.ctxh.volunteer.module.notification.enums;

public enum NotificationType {
    REMINDER,               // Nhắc nhở trước 1h
    ATTENDANCE_COMPLETED,   // Hoàn thành check-in/out
    ENROLLMENT_APPROVED,    // Đăng ký được duyệt
    ENROLLMENT_REJECTED,    // Đăng ký bị từ chối
    ENROLLMENT_CREATED,     // Có người đăng ký hoạt động mới
    ACTIVITY_UPDATED,       // Hoạt động được cập nhật
    ACTIVITY_CANCELLED,     // Hoạt động bị hủy
    GENERAL                 // Thông báo chung
}
