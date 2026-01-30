package com.epam.rd.autocode.spring.project.validation.annotation;

import com.epam.rd.autocode.spring.project.validation.validator.ValidDateRangeConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidDateRangeConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "Start date must be before end date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}