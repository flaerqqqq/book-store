package com.epam.rd.autocode.spring.project.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String msg) {
        super(msg);
    }
}