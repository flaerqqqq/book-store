package com.epam.rd.autocode.spring.project.validation.validator;


import com.epam.rd.autocode.spring.project.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*\\W).{8,64}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}