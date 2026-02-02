package com.epam.rd.autocode.spring.project.exception;

public class IllegalOrderStateException extends RuntimeException {
    public IllegalOrderStateException(String msg) {
        super (msg);
    }
}