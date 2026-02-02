package com.epam.rd.autocode.spring.project.validation.validator;

import java.time.LocalDateTime;

public interface DateTimeRangeAware {

    LocalDateTime getDateTimeFrom();

    LocalDateTime getDateTimeTo();

    String getDateTimeFieldName();
}