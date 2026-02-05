package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeRegisterRequestDto;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @GetMapping("/employees/new")
    public String getEmployeeRegisterPage(Model model) {
        model.addAttribute("employee", new EmployeeRegisterRequestDto());
        return "admin/employee-form";
    }

    @PostMapping("/employees/new")
    public String registerEmployee(@ModelAttribute("employee") @Valid EmployeeRegisterRequestDto requestDto,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/employee-form";
        }

        EmployeeDTO employeeDto = employeeMapper.registerDtoToDto(requestDto);
        employeeService.addEmployee(employeeDto);

        return "redirect:/";
    }
}