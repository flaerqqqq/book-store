package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.AuthTokenResponseDto;
import com.epam.rd.autocode.spring.project.dto.LoginRequestDto;
import com.epam.rd.autocode.spring.project.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        model.addAttribute("login", new LoginRequestDto());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("login") @Valid LoginRequestDto loginRequestDto,
                        BindingResult bindingResult,
                        HttpServletResponse resp) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        AuthTokenResponseDto responseDto = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", responseDto.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
        resp.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        return "redirect:/home";
    }
}