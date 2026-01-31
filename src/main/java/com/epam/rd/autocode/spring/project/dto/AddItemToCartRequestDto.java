package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddItemToCartRequestDto {

    @NotNull(message = "Book public ID is required")
    private UUID bookPublicId;
}