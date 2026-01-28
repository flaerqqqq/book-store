package com.epam.rd.autocode.spring.project.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(UserDetails userDetails);
}