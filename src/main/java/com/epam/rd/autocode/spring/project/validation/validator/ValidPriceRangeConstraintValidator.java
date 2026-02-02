package com.epam.rd.autocode.spring.project.validation.validator;

import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.validation.annotation.ValidPriceRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPriceRangeConstraintValidator implements ConstraintValidator<ValidPriceRange, PriceRangeAware> {
    @Override
    public boolean isValid(PriceRangeAware value, ConstraintValidatorContext context) {
        if (value.getMinPrice() == null || value.getMaxPrice() == null) {
            return true;
        }

        boolean isValid = value.getMinPrice().compareTo(value.getMaxPrice()) <= 0;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("minPrice")
                    .addConstraintViolation();
        }

        return isValid;
    }
}