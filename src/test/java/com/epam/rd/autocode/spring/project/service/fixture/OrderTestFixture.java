package com.epam.rd.autocode.spring.project.service.fixture;


import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderItemDto;
import com.epam.rd.autocode.spring.project.dto.OrderSummaryDto;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.OrderItem;
import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class OrderTestFixture {

    public static final UUID ORDER_PUBLIC_ID = UUID.randomUUID();
    public static final UUID BOOK_PUBLIC_ID = BookTestFixture.DEFAULT_PUBLIC_ID;
    public static final UUID CLIENT_PUBLIC_ID = ClientTestFixture.DEFAULT_PUBLIC_ID;

    public static Order getCreatedOrder() {
        return Order.builder()
                .id(1L)
                .publicId(ORDER_PUBLIC_ID)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ONE)
                .deliveryType(DeliveryType.STANDARD)
                .deliveryAddress("deliveryAddress")
                .comment("comment")
                .status(OrderStatus.CREATED)
                .build();
    }

    public static OrderItem getDefaultOrderItem() {
        return OrderItem.builder()
                .id(1L)
                .bookPublicId(BOOK_PUBLIC_ID)
                .bookName("bookName")
                .priceAtPurchase(BigDecimal.ZERO)
                .quantity(1)
                .subtotal(BigDecimal.ZERO)
                .build();
    }

    public static OrderDTO getCreatedOrderDto() {
        return OrderDTO.builder()
                .publicId(ORDER_PUBLIC_ID)
                .clientPublicId(CLIENT_PUBLIC_ID)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ONE)
                .deliveryType(DeliveryType.STANDARD)
                .deliveryAddress("deliveryAddress")
                .status(OrderStatus.CREATED)
                .comment("comment")
                .orderItems(new ArrayList<>())
                .build();
    }

    public static OrderSummaryDto getCreatedOrderSummaryDto() {
        return OrderSummaryDto.builder()
                .publicId(ORDER_PUBLIC_ID)
                .clientPublicId(CLIENT_PUBLIC_ID)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ONE)
                .deliveryType(DeliveryType.STANDARD)
                .deliveryAddress("deliveryAddress")
                .comment("comment")
                .status(OrderStatus.CREATED)
                .totalItems(1)
                .build();
    }

    public static OrderItemDto getDefaultOrderItemDto() {
        return OrderItemDto.builder()
                .orderPublicId(ORDER_PUBLIC_ID)
                .bookPublicId(BOOK_PUBLIC_ID)
                .bookName("bookName")
                .priceAtPurchase(BigDecimal.ZERO)
                .quantity(1)
                .subtotal(BigDecimal.ZERO)
                .build();
    }
}