package com.epam.rd.autocode.spring.project.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String msg) {
        super(msg);
    }

    public AlreadyExistException(Class<?> entityClass, String fieldName, Object field) {
        super(String.format("%s already exists with %s: %s", entityClass.getSimpleName(), fieldName, field));
    }
}