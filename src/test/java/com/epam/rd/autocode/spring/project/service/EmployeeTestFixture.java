package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;

import java.time.LocalDate;

public class EmployeeTestFixture {

    public static final String DEFAULT_EMAIL = "email@mail.com";

    public static Employee getDefaultEmployee() {
        return Employee.builder()
                .id(1L)
                .name("name")
                .email("email@mail.com")
                .password("password")
                .phone("+123456789")
                .birthDate(LocalDate.now())
                .build();
    }

    public static EmployeeDTO getDefaultEmployeeDto() {
        return EmployeeDTO.builder()
                .name("name")
                .email("email@mail.com")
                .password("password")
                .phone("+123456789")
                .birthDate(LocalDate.now())
                .build();
    }
}