package com.epam.rd.autocode.spring.project.validation.annotation;

import com.epam.rd.autocode.spring.project.validation.validator.ValidDecimalRangeConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidDecimalRangeConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDecimalRange {

    String message() default "Minimum value must be lower than maximum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}