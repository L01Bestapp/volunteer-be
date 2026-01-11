package com.ctxh.volunteer.module.notification.scheduler;

import com.ctxh.volunteer.module.activity.entity.Activity;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import com.ctxh.volunteer.module.enrollment.EnrollmentStatus;
import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import com.ctxh.volunteer.module.enrollment.repository.EnrollmentRepository;
import com.ctxh.volunteer.module.notification.enums.NotificationType;
import com.ctxh.volunteer.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduler to send reminder notifications 1 hour before activity starts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityReminderScheduler {

    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    /**
     * Run every 5 minutes to check for activities starting soon
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void sendActivityReminders() {
        try {
            log.debug("Running activity reminder scheduler");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);
            LocalDateTime fiveMinutesLater = now.plusMinutes(5);

            // Find activities starting within the next hour
            // We use a 5-minute window to avoid missing activities
            List<Activity> upcomingActivities = activityRepository.findActivitiesStartingBetween(
                    now,
                    oneHourLater.plusMinutes(5)
            );

            log.debug("Found {} activities starting within the next hour", upcomingActivities.size());

            for (Activity activity : upcomingActivities) {
                // Get all approved enrollments for this activity
                List<Enrollment> enrollments = enrollmentRepository.findByActivityIdAndStatus(
                        activity.getActivityId(),
                        EnrollmentStatus.APPROVED
                );

                log.debug("Activity '{}' has {} enrolled students", activity.getTitle(), enrollments.size());

                for (Enrollment enrollment : enrollments) {
                    Long userId = enrollment.getStudent().getUser().getUserId();

                    // Check if reminder already sent to avoid spam
                    if (notificationService.hasReminderBeenSent(userId, activity.getActivityId())) {
                        log.debug("Reminder already sent to user {} for activity {}", userId, activity.getActivityId());
                        continue;
                    }

                    // Send reminder notification
                    sendReminderNotification(userId, activity);
                }
            }

        } catch (Exception e) {
            log.error("Error in activity reminder scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminder notification to a user
     */
    private void sendReminderNotification(Long userId, Activity activity) {
        try {
            String title = "Nhắc nhở: Hoạt động tình nguyện sắp bắt đầu";
            String body = String.format(
                    "Hoạt động \"%s\" sẽ bắt đầu vào lúc %s. Hãy chuẩn bị và đến đúng giờ nhé!",
                    activity.getTitle(),
                    formatDateTime(activity.getStartDateTime())
            );

            Map<String, Object> data = new HashMap<>();
            data.put("activityId", activity.getActivityId());
            data.put("activityTitle", activity.getTitle());
            data.put("startDateTime", activity.getStartDateTime().toString());
            data.put("address", activity.getAddress());

            notificationService.sendAndSaveNotification(
                    userId,
                    title,
                    body,
                    NotificationType.REMINDER,
                    data
            );

            log.info("Sent reminder notification to user {} for activity {}", userId, activity.getActivityId());

        } catch (Exception e) {
            log.error("Failed to send reminder notification to user {} for activity {}: {}",
                    userId, activity.getActivityId(), e.getMessage());
        }
    }

    /**
     * Format LocalDateTime to readable string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return String.format("%02d:%02d ngày %02d/%02d/%d",
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue(),
                dateTime.getYear()
        );
    }
}
