package com.epam.rd.autocode.spring.project.service.fixture;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.model.Employee;

import java.time.LocalDate;
import java.util.UUID;

public class EmployeeTestFixture {

    public static final String DEFAULT_EMAIL = "email@mail.com";
    public static final UUID DEFAULT_PUBLIC_ID = UUID.randomUUID();

    public static Employee getDefaultEmployee() {
        return Employee.builder()
                .id(1L)
                .publicId(DEFAULT_PUBLIC_ID)
                .name("name")
                .email(DEFAULT_EMAIL)
                .password("password")
                .phone("+123456789")
                .birthDate(LocalDate.now())
                .build();
    }

    public static EmployeeDTO getDefaultEmployeeDto() {
        return EmployeeDTO.builder()
                .name("name")
                .publicId(DEFAULT_PUBLIC_ID)
                .email(DEFAULT_EMAIL)
                .password("password")
                .phone("+123456789")
                .birthDate(LocalDate.now())
                .build();
    }
}