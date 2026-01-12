package com.ctxh.volunteer.module.notification.event;

import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when a student enrolls in an activity
 */
@Getter
public class EnrollmentCreatedEvent extends ApplicationEvent {

    private final Enrollment enrollment;

    public EnrollmentCreatedEvent(Object source, Enrollment enrollment) {
        super(source);
        this.enrollment = enrollment;
    }
}
