package com.epam.rd.autocode.spring.project.validation.validator;

import java.time.LocalDate;

public interface DateRangeAware{

    LocalDate getDateFrom();

    LocalDate getDateTo();

    String getDateFieldName();
}