package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegisterRequestDto;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        ClientRegisterRequestDto requestDTO  = new ClientRegisterRequestDto();
        model.addAttribute("client", requestDTO);
        return "register";
    }

    @PostMapping("/register")
    public void registerClient(@ModelAttribute("client") ClientRegisterRequestDto requestDto,
                                 BindingResult bindingResult) {
        ClientDTO clientDto = clientMapper.registerDtoToDto(requestDto);
        clientService.addClient(clientDto);
    }
}