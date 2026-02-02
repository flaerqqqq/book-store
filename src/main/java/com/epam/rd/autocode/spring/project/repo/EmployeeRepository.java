package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    Long deleteByEmail(String email);

    Optional<Employee> findByPublicId(UUID publicId);
}