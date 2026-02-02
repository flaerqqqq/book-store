package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartResponseDto {

    private UUID publicId;

    private UUID clientPublicId;

    private BigDecimal totalAmount;

    private Page<ShoppingCartItemDto> cartItems;
}