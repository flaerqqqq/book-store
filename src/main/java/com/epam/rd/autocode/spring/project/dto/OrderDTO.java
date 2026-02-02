package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private UUID publicId;

    private UUID clientPublicId;

    private UUID employeePublicId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private List<OrderItemDto> orderItems;
}