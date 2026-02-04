package com.epam.rd.autocode.spring.project.service.fixture;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;

import java.math.BigDecimal;
import java.util.UUID;

public class ClientTestFixture {

    public static final String DEFAULT_EMAIL = "email@mail.com";
    public static final UUID DEFAULT_PUBLIC_ID = UUID.randomUUID();

    public static Client getDefaultClient() {
        return Client.builder()
                .id(1L)
                .publicId(DEFAULT_PUBLIC_ID)
                .name("name")
                .email(DEFAULT_EMAIL)
                .password("password")
                .balance(BigDecimal.ZERO)
                .build();
    }

    public static ClientDTO getDefaultClientDto() {
        return ClientDTO.builder()
                .publicId(DEFAULT_PUBLIC_ID)
                .name("name")
                .email(DEFAULT_EMAIL)
                .password("password")
                .balance(BigDecimal.ZERO)
                .build();
    }
}