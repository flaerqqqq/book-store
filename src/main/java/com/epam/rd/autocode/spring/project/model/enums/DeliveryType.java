package com.epam.rd.autocode.spring.project.model.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum DeliveryType {

    STANDARD(new BigDecimal("5.00")),
    EXPRESS(new BigDecimal("10.00")),
    PICKUP(BigDecimal.ZERO);

    private final BigDecimal baseCost;

    DeliveryType(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }
}