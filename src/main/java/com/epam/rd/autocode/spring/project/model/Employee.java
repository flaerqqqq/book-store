package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends User {

    @Column(nullable = false)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    public Employee(String email, String password, String name, String phone, LocalDate birthDate) {
        super(email, password, name);
        changePhone(phone);
        changeBirthDate(birthDate);
    }

    public void changePhone(String phone) {
        this.phone = Objects.requireNonNull(phone, "Phone must not be null");
    }

    public void changeBirthDate(LocalDate birthDate) {
        Objects.requireNonNull(birthDate, "Birth date must not be null");

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date must be in the past");
        }

        if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Employee must be at least 18 years old");
        }

        this.birthDate = birthDate;
    }
}