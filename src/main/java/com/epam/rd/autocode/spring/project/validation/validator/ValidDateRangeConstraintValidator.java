package com.epam.rd.autocode.spring.project.validation.validator;

import com.epam.rd.autocode.spring.project.validation.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeConstraintValidator implements ConstraintValidator<ValidDateRange, DateRangeAware> {
    @Override
    public boolean isValid(DateRangeAware value, ConstraintValidatorContext context) {
        if (value.getDateFrom() == null || value.getDateTo() == null) {
            return true;
        }

        boolean isValid = value.getDateFrom().isBefore(value.getDateTo());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("dateFrom")
                    .addConstraintViolation();
        }

        return isValid;
    }
}