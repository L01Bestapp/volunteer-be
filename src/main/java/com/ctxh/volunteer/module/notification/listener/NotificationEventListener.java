package com.ctxh.volunteer.module.notification.listener;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.attendance.entity.Attendance;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.event.AttendanceCompletedEvent;
import com.ctxh.volunteer.module.notification.event.EnrollmentCreatedEvent;
import com.ctxh.volunteer.module.notification.service.NotificationService;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.student.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for notification events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handle attendance completed event
     */
    @Async
    @EventListener
    public void handleAttendanceCompleted(AttendanceCompletedEvent event) {
        try {
            Attendance attendance = event.getAttendance();
            Student student = attendance.getStudent();
            Activity activity = attendance.getActivity();

            // Get user ID
            Long userId = student.getUser().getUserId();

            // Get CTXH days
            Double ctxhDays = activity.getTheNumberOfCtxhDay();

            // Prepare notification content
            String title = "Hoàn thành hoạt động tình nguyện";
            String body = String.format(
                    "Bạn đã hoàn thành hoạt động \"%s\". Bạn được cộng %.1f ngày CTXH.",
                    activity.getTitle(),
                    ctxhDays
            );

            // Prepare additional data
            Map<String, Object> data = new HashMap<>();
            data.put("activityId", activity.getActivityId());
            data.put("activityTitle", activity.getTitle());
            data.put("ctxhDays", ctxhDays);
            data.put("attendanceId", attendance.getAttendanceId());

            // Send and save notification
            notificationService.sendAndSaveNotification(
                    userId,
                    title,
                    body,
                    NotificationType.ATTENDANCE_COMPLETED,
                    data
            );

            log.info("Sent attendance completed notification to user {}", userId);

        } catch (Exception e) {
            log.error("Error handling attendance completed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle enrollment created event
     */
    @Async
    @EventListener
    public void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        try {
            Enrollment enrollment = event.getEnrollment();
            Student student = enrollment.getStudent();
            Activity activity = enrollment.getActivity();
            Organization organization = activity.getOrganization();

            // Get organization user ID
            Long organizationUserId = organization.getUser().getUserId();

            // Prepare notification content
            String title = "Có người đăng ký hoạt động mới";
            String body = String.format(
                    "Sinh viên %s (%s) đã đăng ký tham gia hoạt động \"%s\".",
                    student.getFullName(),
                    student.getMssv(),
                    activity.getTitle()
            );

            // Prepare additional data
            Map<String, Object> data = new HashMap<>();
            data.put("enrollmentId", enrollment.getEnrollmentId());
            data.put("activityId", activity.getActivityId());
            data.put("activityTitle", activity.getTitle());
            data.put("studentId", student.getStudentId());
            data.put("studentName", student.getFullName());
            data.put("studentMssv", student.getMssv());

            // Send and save notification
            notificationService.sendAndSaveNotification(
                    organizationUserId,
                    title,
                    body,
                    NotificationType.ENROLLMENT_CREATED,
                    data
            );

            log.info("Sent enrollment created notification to organization user {}", organizationUserId);

        } catch (Exception e) {
            log.error("Error handling enrollment created event: {}", e.getMessage(), e);
        }
    }
}
