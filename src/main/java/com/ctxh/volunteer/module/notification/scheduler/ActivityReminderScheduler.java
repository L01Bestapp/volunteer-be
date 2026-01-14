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
     * Run every 1 minute to check for activities starting soon
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void sendActivityReminders() {
        try {
            log.info("=== Running activity reminder scheduler at {} ===", LocalDateTime.now());

            LocalDateTime now = LocalDateTime.now();
            // Window: send reminder for activities starting in 55-65 minutes
            LocalDateTime reminderWindowStart = now.plusMinutes(55);
            LocalDateTime reminderWindowEnd = now.plusMinutes(65);

            log.info("Searching for activities starting between {} and {}",
                    reminderWindowStart, reminderWindowEnd);

            // Find activities starting within the reminder window
            List<Activity> upcomingActivities = activityRepository.findActivitiesStartingBetween(
                    now,
                    reminderWindowEnd
            );

            log.info("Found {} activities in reminder window", upcomingActivities.size());

            if (upcomingActivities.isEmpty()) {
                log.warn("⚠️ No activities found in reminder window!");
                log.warn("Debug info:");
                log.warn("  - Current time: {}", now);
                log.warn("  - Searching for activities starting between {} and {}", reminderWindowStart, reminderWindowEnd);
                log.warn("  - Query is looking for activities with status IN ('UPCOMING', 'ONGOING')");
                log.warn("Please check: 1) Activity exists? 2) Activity status is UPCOMING or ONGOING? 3) startDateTime is in the window?");
                return;
            }

            log.info("Activities found:");
            for (Activity act : upcomingActivities) {
                log.info("  - '{}' (ID: {}, Status: {}, Start: {})",
                        act.getTitle(), act.getActivityId(), act.getActivityStatus(), act.getStartDateTime());
            }

            for (Activity activity : upcomingActivities) {
                log.info("Processing activity: '{}' (ID: {}) starting at {}",
                        activity.getTitle(), activity.getActivityId(), activity.getStartDateTime());

                // Get all approved enrollments for this activity
                List<Enrollment> enrollments = enrollmentRepository.findByActivityIdAndStatus(
                        activity.getActivityId(),
                        EnrollmentStatus.APPROVED
                );

                log.info("Activity '{}' has {} approved enrollments", activity.getTitle(), enrollments.size());

                if (enrollments.isEmpty()) {
                    log.info("No approved enrollments for activity '{}', skipping", activity.getTitle());
                    continue;
                }

                int remindersSent = 0;
                int remindersSkipped = 0;

                for (Enrollment enrollment : enrollments) {
                    Long userId = enrollment.getStudent().getUser().getUserId();

                    // Check if reminder already sent to avoid spam
                    if (notificationService.hasReminderBeenSent(userId, activity.getActivityId())) {
                        log.debug("Reminder already sent to user {} for activity {}", userId, activity.getActivityId());
                        remindersSkipped++;
                        continue;
                    }

                    // Send reminder notification
                    sendReminderNotification(userId, activity);
                    remindersSent++;
                }

                log.info("Activity '{}': Sent {} reminders, Skipped {} (already sent)",
                        activity.getTitle(), remindersSent, remindersSkipped);
            }

            log.info("=== Activity reminder scheduler completed ===");

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
