package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

    private UUID publicId;

    private String email;

    private String password;

    private String name;

    private BigDecimal balance;

    private Set<String> roles;
}