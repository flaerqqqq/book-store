package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.RoleRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        log.info("Creating new employee with email: {}", employee.getEmail());
        Objects.requireNonNull(employee, "Employee data must not be null");

        if (employeeRepository.existsByEmail(employee.getEmail())) {
            log.warn("Failed to add employee: email {} already exists", employee.getEmail());
            throw new AlreadyExistException(Employee.class, "email", employee.getEmail());
        }

        Role employeeRole = roleRepository.findByName(Role.RoleName.ROLE_EMPLOYEE).orElseThrow(() -> {
            log.error("Critical error: ROLE_EMPLOYEE not found in database");
            return new NotFoundException(Role.class, "name", Role.RoleName.ROLE_EMPLOYEE);
        });

        String passwordHash = passwordEncoder.encode(employee.getPassword());
        Employee employeeEntity = employeeMapper.dtoToEntity(employee);
        employeeEntity.setPassword(passwordHash);
        employeeEntity.getRoles().add(employeeRole);

        Employee savedEmployee = employeeRepository.save(employeeEntity);
        log.info("Employee successfully created with public ID: {}", savedEmployee.getPublicId());

        return employeeMapper.entityToDto(savedEmployee);
    }
}