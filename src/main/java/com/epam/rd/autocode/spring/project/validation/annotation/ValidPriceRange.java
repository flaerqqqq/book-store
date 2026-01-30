package com.epam.rd.autocode.spring.project.validation.annotation;

import com.epam.rd.autocode.spring.project.validation.validator.ValidPriceRangeConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidPriceRangeConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPriceRange {

    String message() default "Minimum price must be lower than maximum";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}