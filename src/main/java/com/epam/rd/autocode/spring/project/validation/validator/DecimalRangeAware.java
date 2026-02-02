package com.epam.rd.autocode.spring.project.validation.validator;

import java.math.BigDecimal;

public interface DecimalRangeAware {

    BigDecimal getMin();

    BigDecimal getMax();

    String getDecimalFieldName();
}