package com.epam.rd.autocode.spring.project.validation.validator;


import com.epam.rd.autocode.spring.project.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*\\W).{8,64}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (password == null || password.isBlank()) {
            addViolation(context, "Password is required");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            addViolation(context, "Password must be 8-64 characters long and contain at least one letter, one number, and one special character");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}