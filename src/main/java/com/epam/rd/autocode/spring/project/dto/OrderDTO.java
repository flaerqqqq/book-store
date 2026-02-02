package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
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

    private DeliveryType deliveryType;

    private String deliveryAddress;

    private String comment;

    private OrderStatus status;

    private UUID canceledByPublicId;

    private String reason;

    private LocalDateTime canceledAt;

    private List<OrderItemDto> orderItems;
}