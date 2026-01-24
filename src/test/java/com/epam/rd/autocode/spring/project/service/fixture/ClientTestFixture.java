package com.epam.rd.autocode.spring.project.service.fixture;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;

import java.math.BigDecimal;

public class ClientTestFixture {

    public static final String DEFAULT_EMAIL = "email@mail.com";

    public static Client getDefaultClient() {
        return Client.builder()
                .id(1L)
                .name("name")
                .email("email@mail.com")
                .password("password")
                .balance(BigDecimal.ZERO)
                .build();
    }

    public static ClientDTO getDefaultClientDto() {
        return ClientDTO.builder()
                .name("name")
                .email("email@mail.com")
                .password("password")
                .balance(BigDecimal.ZERO)
                .build();
    }
}