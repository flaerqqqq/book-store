package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartItemDto {

    private Long id;

    private UUID cartPublicId;

    private UUID bookPublicId;

    private Integer quantity;

    private BigDecimal priceAtAdd;

    private BigDecimal subtotal;
}