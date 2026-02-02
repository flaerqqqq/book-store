package com.epam.rd.autocode.spring.project.validation.validator;

import com.epam.rd.autocode.spring.project.validation.annotation.ValidDecimalRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDecimalRangeConstraintValidator implements ConstraintValidator<ValidDecimalRange, DecimalRangeAware> {
    @Override
    public boolean isValid(DecimalRangeAware value, ConstraintValidatorContext context) {
        if (value.getMin() == null || value.getMax() == null) {
            return true;
        }

        boolean isValid = value.getMin().compareTo(value.getMax()) <= 0;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(value.getDecimalFieldName())
                    .addConstraintViolation();
        }

        return isValid;
    }
}