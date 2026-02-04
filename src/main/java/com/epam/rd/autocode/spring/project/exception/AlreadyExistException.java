package com.epam.rd.autocode.spring.project.exception;

import lombok.Getter;

@Getter
public class AlreadyExistException extends RuntimeException {
    private Class<?> entityClass;

    public AlreadyExistException(String msg) {
        super(msg);
    }

    public AlreadyExistException(Class<?> entityClass, String fieldName, Object field) {
        super(String.format("%s already exists with %s: %s", entityClass.getSimpleName(), fieldName, field));
        this.entityClass = entityClass;
    }
}