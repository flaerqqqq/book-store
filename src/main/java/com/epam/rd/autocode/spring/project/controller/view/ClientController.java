package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientRegisterRequestDto;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @GetMapping("/register")
    public String getRegisterPage(Authentication authentication,
                                  Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        model.addAttribute("client", new ClientRegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerClient(@ModelAttribute("client") @Valid ClientRegisterRequestDto requestDto,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/auth/register";
        }

        ClientDTO clientDto = clientMapper.registerDtoToDto(requestDto);
        clientService.addClient(clientDto);

        return "redirect:/login";
    }
}