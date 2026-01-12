package com.ctxh.volunteer.module.notification.event;

import com.ctxh.volunteer.module.enrollment.entity.Enrollment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when an enrollment is rejected by organization
 */
@Getter
public class EnrollmentRejectedEvent extends ApplicationEvent {

    private final Enrollment enrollment;

    public EnrollmentRejectedEvent(Object source, Enrollment enrollment) {
        super(source);
        this.enrollment = enrollment;
    }
}
