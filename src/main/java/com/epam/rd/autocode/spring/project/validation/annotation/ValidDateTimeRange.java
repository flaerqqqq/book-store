package com.epam.rd.autocode.spring.project.validation.annotation;

import com.epam.rd.autocode.spring.project.validation.validator.ValidDateTimeRangeConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidDateTimeRangeConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateTimeRange {

    String message() default "Start date and time must be before end date and time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}