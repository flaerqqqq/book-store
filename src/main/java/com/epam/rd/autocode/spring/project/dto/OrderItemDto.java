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
public class OrderItemDto {

    private UUID publicId;

    private UUID orderPublicId;

    private UUID bookPublicId;

    private String bookName;

    private BigDecimal priceAtPurchase;

    private Integer quantity;

    private BigDecimal subtotal;
}