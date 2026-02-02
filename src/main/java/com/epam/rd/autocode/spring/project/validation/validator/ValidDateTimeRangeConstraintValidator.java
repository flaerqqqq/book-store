package com.epam.rd.autocode.spring.project.validation.validator;

import com.epam.rd.autocode.spring.project.validation.annotation.ValidDateTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateTimeRangeConstraintValidator implements ConstraintValidator<ValidDateTimeRange, DateTimeRangeAware> {
    @Override
    public boolean isValid(DateTimeRangeAware value, ConstraintValidatorContext context) {
        if (value.getDateTimeFrom() == null || value.getDateTimeTo() == null) {
            return true;
        }

        boolean isValid = value.getDateTimeFrom().isBefore(value.getDateTimeTo());

        if (!isValid) {
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(value.getDateTimeFieldName())
                    .addConstraintViolation();
        }

        return isValid;
    }
}