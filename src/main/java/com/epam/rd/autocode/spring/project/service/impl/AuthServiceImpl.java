package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.AuthTokenResponseDto;
import com.epam.rd.autocode.spring.project.service.AuthService;
import com.epam.rd.autocode.spring.project.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Override
    public AuthTokenResponseDto login(String email, String password) {
        log.debug("Login attempt for user: {}", email);

        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(password, "Password must not be null");

        UserDetails userDetails = authenticate(email, password);

        String accessToken = jwtService.generateAccessToken(userDetails);

        log.info("User {} logged in successfully", email);

        return AuthTokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
    }

    private UserDetails authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username, password
        );

        Authentication authentication = authManager.authenticate(authToken);

        log.debug("Authentication successful for: {}", username);

        return (UserDetails) authentication.getPrincipal();
    }
}