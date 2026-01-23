package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    public void setUp() {
        employeeService = new EmployeeServiceImpl(
                employeeRepository,
                employeeMapper,
                passwordEncoder
        );
    }

    @Test
    void getAllEmployees_ShouldReturnSpecifiedPage_WhenPageableIsPresent() {
        Employee employee = EmployeeTestFixture.getDefaultEmployee();
        Pageable inputPageable = Pageable.ofSize(1);

        Page<Employee> foundEmployeePage = new PageImpl<>(Collections.singletonList(employee), inputPageable, 1);

        when(employeeRepository.findAll(inputPageable)).thenReturn(foundEmployeePage);

        Page<EmployeeDTO> actualEmployeePage = employeeService.getAllEmployees(inputPageable);

        assertThat(actualEmployeePage).isNotNull();
        assertThat(actualEmployeePage.getTotalElements()).isEqualTo(1);
        assertThat(actualEmployeePage.getContent()).hasSize(1);

        EmployeeDTO actualEmployeeDto = actualEmployeePage.getContent().get(0);
        assertThat(actualEmployeeDto.getEmail()).isEqualTo(employee.getEmail());

        verify(employeeRepository, times(1)).findAll(inputPageable);
    }

    @Test
    void getAllEmployees_ShouldReturnDefaultPage_WhenPageableIsNotPresent() {
        int expectedDefaultSize = 10;
        Pageable inputPageable = null;

        Page<Employee> foundEmployeePage = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(foundEmployeePage);

        employeeService.getAllEmployees(inputPageable);

        ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository, times(1)).findAll(pageableArgCaptor.capture());

        Pageable actualPageable = pageableArgCaptor.getValue();

        assertThat(actualPageable).isNotNull();
        assertThat(actualPageable.getPageSize()).isEqualTo(expectedDefaultSize);
    }

    @Test
    void getEmployeeByEmail_ShouldReturnActualEmployee_WhenEmployeeWithSuchEmailExists() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        Employee foundEmployee = EmployeeTestFixture.getDefaultEmployee();

        when(employeeRepository.findByEmail(inputEmail)).thenReturn(Optional.of(foundEmployee));

        EmployeeDTO actualEmployeeDto = employeeService.getEmployeeByEmail(inputEmail);

        assertThat(actualEmployeeDto).isNotNull();
        assertThat(actualEmployeeDto.getEmail()).isEqualTo(inputEmail);
        assertThat(actualEmployeeDto.getName()).isEqualTo(foundEmployee.getName());

        verify(employeeRepository, times(1)).findByEmail(inputEmail);
    }

    @Test
    void getEmployeeByEmail_ShouldThrowNotFound_WhenEmployeeWithSuchEmailDoesNotExist() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;

        when(employeeRepository.findByEmail(inputEmail)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                employeeService.getEmployeeByEmail(inputEmail));
        assertThat(thrown.getMessage()).contains(inputEmail);

        verify(employeeRepository, times(1)).findByEmail(inputEmail);
    }

    @Test
    void getEmployeeByEmail_ShouldThrowNullPointer_WhenInputEmailIsNotPresent() {
        String inputEmail = null;
        assertThrows(NullPointerException.class, () ->
                employeeService.getEmployeeByEmail(inputEmail));
    }

    @Test
    void updateEmployeeByEmail_ShouldSaveAndReturnUpdatedEmployee_WhenEmployeeExistsByEmail() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        EmployeeDTO inputEmployeeDto = EmployeeTestFixture.getDefaultEmployeeDto();
        Employee foundEmployee = EmployeeTestFixture.getDefaultEmployee();

        when(employeeRepository.findByEmail(inputEmail)).thenReturn(Optional.of(foundEmployee));
        when(employeeRepository.save(foundEmployee)).thenReturn(foundEmployee);

        EmployeeDTO actualEmployeeDto = employeeService.updateEmployeeByEmail(inputEmail, inputEmployeeDto);

        assertThat(actualEmployeeDto).isNotNull();
        assertThat(actualEmployeeDto.getEmail()).isEqualTo(inputEmail);
        assertThat(actualEmployeeDto.getName()).isEqualTo(inputEmployeeDto.getName());

        verify(employeeRepository, times(1)).findByEmail(inputEmail);
        verify(employeeRepository, times(1)).save(foundEmployee);
    }

    @Test
    void updateEmployeeByEmail_ShouldThrowNotFound_WhenEmployeeDoesNotExistByEmail() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        EmployeeDTO inputEmployeeDto = EmployeeDTO.builder().build();

        when(employeeRepository.findByEmail(inputEmail)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                employeeService.updateEmployeeByEmail(inputEmail, inputEmployeeDto));
        assertThat(thrown.getMessage()).contains(inputEmail);
    }

    @Test
    void updateEmployeeByEmail_ShouldThrowNullPointer_WhenInputEmailIsNull() {
        String inputEmail = null;
        EmployeeDTO inputEmployeeDto = EmployeeDTO.builder().build();

        assertThrows(NullPointerException.class, () ->
                employeeService.updateEmployeeByEmail(inputEmail, inputEmployeeDto));
    }

    @Test
    void updateEmployeeByEmail_ShouldThrowNullPointer_WhenInputEmployeeDtoIsNull() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        EmployeeDTO inputEmployeeDto = null;

        assertThrows(NullPointerException.class, () ->
                employeeService.updateEmployeeByEmail(inputEmail, inputEmployeeDto));
    }

    @Test
    void deleteEmployeeByEmail_ShouldDelete_WhenEmployeeExistsByEmail() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        Long deletedCount = 1L;

        when (employeeRepository.deleteByEmail(inputEmail)).thenReturn(deletedCount);

        employeeService.deleteEmployeeByEmail(inputEmail);

        verify(employeeRepository, times(1)).deleteByEmail(inputEmail);
    }

    @Test
    void deleteEmployeeByEmail_ShouldThrowNotFound_WhenEmployeeDoesNotExistByEmail() {
        String inputEmail = EmployeeTestFixture.DEFAULT_EMAIL;
        Long deletedCount = 0L;

        when (employeeRepository.deleteByEmail(inputEmail)).thenReturn(deletedCount);

        NotFoundException thrown = assertThrows(NotFoundException.class, () ->
                employeeService.deleteEmployeeByEmail(inputEmail));
        assertThat(thrown.getMessage()).contains(inputEmail);

        verify(employeeRepository, times(1)).deleteByEmail(inputEmail);
    }

    @Test
    void deleteEmployeeByEmail_ShouldThrowNullPointer_WhenInputEmailIsNull() {
        String inputEmail = null;
        assertThrows(NullPointerException.class, () ->
                employeeService.deleteEmployeeByEmail(inputEmail));
    }

    @Test
    void addEmployee_ShouldSaveEntityWithHashedPassword() {
        EmployeeDTO inputEmployeeDto = EmployeeTestFixture.getDefaultEmployeeDto();
        Employee savedEmployee = EmployeeTestFixture.getDefaultEmployee();
        String employeeEmail = inputEmployeeDto.getEmail();
        String passwordHash = "passwordHash";

        when(employeeRepository.existsByEmail(employeeEmail)).thenReturn(false);
        when(passwordEncoder.encode(inputEmployeeDto.getPassword())).thenReturn(passwordHash);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        employeeService.addEmployee(inputEmployeeDto);

        ArgumentCaptor<Employee> employeeArgCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository, times(1)).save(employeeArgCaptor.capture());

        Employee savedArgEmployee = employeeArgCaptor.getValue();
        assertThat(savedArgEmployee).isNotNull();
        assertThat(savedArgEmployee.getPassword()).isEqualTo(passwordHash);

        verify(employeeRepository, times(1)).existsByEmail(employeeEmail);
        verify(passwordEncoder, times(1)).encode(inputEmployeeDto.getPassword());
    }

    @Test
    void addEmployee_ShouldReturnCorrectDto_WhenInputIsValid() {
        EmployeeDTO inputEmployeeDto = EmployeeTestFixture.getDefaultEmployeeDto();
        Employee savedEmployee = EmployeeTestFixture.getDefaultEmployee();
        String employeeEmail = inputEmployeeDto.getEmail();
        String passwordHash = "passwordHash";

        when(employeeRepository.existsByEmail(employeeEmail)).thenReturn(false);
        when(passwordEncoder.encode(inputEmployeeDto.getPassword())).thenReturn(passwordHash);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        EmployeeDTO actualEmployeeDto = employeeService.addEmployee(inputEmployeeDto);

        assertThat(actualEmployeeDto).isNotNull();
        assertThat(actualEmployeeDto.getEmail()).isEqualTo(employeeEmail);
    }

    @Test
    void addEmployee_ShouldThrowNullPointer_WhenInputIsNull() {
        EmployeeDTO inputEmployeeDto = null;
        assertThrows(NullPointerException.class, () ->
                employeeService.addEmployee(inputEmployeeDto));
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