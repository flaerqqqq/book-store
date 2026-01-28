package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.AuthTokenResponseDto;
import com.epam.rd.autocode.spring.project.dto.LoginRequestDto;

public interface AuthService {

    AuthTokenResponseDto login(String email, String password);
}