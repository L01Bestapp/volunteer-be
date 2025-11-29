package com.ctxh.volunteer.common.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class EnumConstraintValidator implements ConstraintValidator<EnumValidation, String> {
    List<String> validValues;

    @Override
    public void initialize(EnumValidation constraintAnnotation) {
        validValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants()).map(Enum::name).toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return validValues.contains(value.toUpperCase());
    }
}
