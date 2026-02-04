package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.RoleRepository;
import com.epam.rd.autocode.spring.project.service.fixture.EmployeeTestFixture;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    private EmployeeServiceImpl employeeService;

    private final Role.RoleName EMPLOYEE_ROLE_NAME = Role.RoleName.ROLE_EMPLOYEE;

    private String employeeEmail;
    private Employee employeeEntity;
    private EmployeeDTO employeeDto;

    @BeforeEach
    public void setUpFixture() {
        employeeEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        employeeEntity = EmployeeTestFixture.getDefaultEmployee();
        employeeDto = EmployeeTestFixture.getDefaultEmployeeDto();
    }

    @BeforeEach
    public void setUpService() {
        employeeService = new EmployeeServiceImpl(
                employeeRepository,
                roleRepository,
                employeeMapper,
                passwordEncoder
        );
    }

    @Test
    void addEmployee_ShouldSaveEntityWithHashedPassword() {
        String passwordHash = "passwordHash";
        Role role = new Role(1L, EMPLOYEE_ROLE_NAME);
        when(roleRepository.findByName(EMPLOYEE_ROLE_NAME)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(employeeDto.getPassword())).thenReturn(passwordHash);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeEntity);

        employeeService.addEmployee(employeeDto);

        ArgumentCaptor<Employee> employeeArgCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(1)).save(employeeArgCaptor.capture());

        Employee savedArgEmployee = employeeArgCaptor.getValue();
        assertThat(savedArgEmployee).isNotNull();
        assertThat(savedArgEmployee.getPassword()).isEqualTo(passwordHash);
    }

    @Test
    void addEmployee_ShouldThrowNotFound_WhenRoleNotFound() {
        when (roleRepository.findByName(EMPLOYEE_ROLE_NAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.addEmployee(employeeDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(EMPLOYEE_ROLE_NAME.toString());
    }

    @Test
    void addEmployee_ShouldReturnCorrectDto_WhenInputIsValid() {
        String passwordHash = "passwordHash";
        Role role = new Role(1L, EMPLOYEE_ROLE_NAME);

        when(roleRepository.findByName(EMPLOYEE_ROLE_NAME)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(employeeDto.getPassword())).thenReturn(passwordHash);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeEntity);

        EmployeeDTO actualEmployeeDto = employeeService.addEmployee(employeeDto);

        assertThat(actualEmployeeDto).isNotNull();
        assertThat(actualEmployeeDto.getEmail()).isEqualTo(employeeEmail);
    }

    @Test
    void addEmployee_ShouldThrowNullPointer_WhenInputIsNull() {
        assertThrows(NullPointerException.class, () ->
                employeeService.addEmployee(null));
    }

    @Test
    void addEmployee_ShouldThrowAlreadyExistsOnEmailDuplicate() {
        String employeeEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        EmployeeDTO inputEmployeeDto = EmployeeDTO.builder().email(employeeEmail).build();

        when(employeeRepository.existsByEmail(employeeEmail)).thenReturn(true);

        AlreadyExistException thrown = assertThrows(AlreadyExistException.class, () ->
                employeeService.addEmployee(inputEmployeeDto));
        assertThat(thrown.getMessage()).contains(employeeEmail);
    }
}