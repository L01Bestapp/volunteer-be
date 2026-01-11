package com.ctxh.volunteer.module.notification.event;

import com.ctxh.volunteer.module.attendance.entity.Attendance;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when a student completes attendance (check-out)
 */
@Getter
public class AttendanceCompletedEvent extends ApplicationEvent {

    private final Attendance attendance;

    public AttendanceCompletedEvent(Object source, Attendance attendance) {
        super(source);
        this.attendance = attendance;
    }
}
