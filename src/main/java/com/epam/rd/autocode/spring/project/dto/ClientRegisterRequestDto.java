package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRegisterRequestDto {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword(message = "Password must follow next rules: min size 8, max size 64, at least 1 letter, at least 1 number, at least 1 symbol")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 3)
    private String name;
}