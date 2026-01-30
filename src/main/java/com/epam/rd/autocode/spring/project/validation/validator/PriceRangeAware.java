package com.epam.rd.autocode.spring.project.validation.validator;

import java.math.BigDecimal;

public interface PriceRangeAware {

    BigDecimal getMinPrice();

    BigDecimal getMaxPrice();
}