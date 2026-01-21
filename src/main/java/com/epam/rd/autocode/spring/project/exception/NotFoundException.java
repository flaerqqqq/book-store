package com.epam.rd.autocode.spring.project.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(Class<?> entityClass, String fieldName, Object field) {
        super(String.format("%s can't be found with %s: %s", entityClass.getSimpleName(), fieldName, field));
    }
}