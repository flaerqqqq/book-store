package com.epam.rd.autocode.spring.project.validation.validator;

import com.epam.rd.autocode.spring.project.dto.OrderRequestDto;
import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.validation.annotation.RequiredForDelivery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredForDeliveryConstraintValidator implements ConstraintValidator<RequiredForDelivery, OrderRequestDto> {
    @Override
    public boolean isValid(OrderRequestDto value, ConstraintValidatorContext context) {
        if (value.getDeliveryType() == null) {
            return true;
        }

        if (value.getDeliveryType() != DeliveryType.PICKUP && (value.getDeliveryAddress() == null
                || value.getDeliveryAddress().isBlank())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The address is required when choosing {deliveryType}")
                    .addPropertyNode("deliveryAddress")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}