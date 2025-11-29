package com.ctxh.volunteer.common.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidation {
    String name();
    String message() default "Value is not valid for enum {name}";
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
