package com.epam.rd.autocode.spring.project.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal currentBalance;

    private final BigDecimal requiredFunds;

    public InsufficientFundsException(String msg, BigDecimal currentBalance, BigDecimal requiredFunds) {
        super(msg);
        this.currentBalance = currentBalance;
        this.requiredFunds = requiredFunds;
    }
}