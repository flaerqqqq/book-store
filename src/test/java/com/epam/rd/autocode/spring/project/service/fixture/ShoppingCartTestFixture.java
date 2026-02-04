package com.epam.rd.autocode.spring.project.service.fixture;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.*;

import java.math.BigDecimal;
import java.util.UUID;

public class ShoppingCartTestFixture {

    public static final UUID CART_PUBLIC_ID = UUID.randomUUID();
    public static final UUID CART_ITEM_PUBLIC_ID = UUID.randomUUID();
    public static final UUID CLIENT_PUBLIC_ID = UUID.randomUUID();
    public static final BigDecimal PRICE = BigDecimal.ONE;
    public static final BigDecimal TOTAL_AMOUNT = BigDecimal.ONE;

    public static ShoppingCartItem getDefaultCartItem() {
        return ShoppingCartItem.builder()
                .id(1L)
                .publicId(CART_ITEM_PUBLIC_ID)
                .book(BookTestFixture.getDefaultBook())
                .quantity(1)
                .priceAtAdd(PRICE)
                .subtotal(TOTAL_AMOUNT)
                .build();
    }

    public static ShoppingCart getDefaultCart() {
        return ShoppingCart.builder()
                .id(1L)
                .publicId(CART_PUBLIC_ID)
                .totalAmount(TOTAL_AMOUNT)
                .client(ClientTestFixture.getDefaultClient())
                .build();
    }

    public static ShoppingCartItemDto getDefaultCartItemDto() {
        return ShoppingCartItemDto.builder()
                .publicId(CART_ITEM_PUBLIC_ID)
                .cartPublicId(CART_PUBLIC_ID)
                .book(BookTestFixture.getDefaultBookDto())
                .quantity(1)
                .priceAtAdd(PRICE)
                .subtotal(TOTAL_AMOUNT)
                .build();
    }

    public static ShoppingCartDto getDefaultCartDto() {
        return ShoppingCartDto.builder()
                .publicId(CART_PUBLIC_ID)
                .totalAmount(TOTAL_AMOUNT)
                .clientPublicId(CLIENT_PUBLIC_ID)
                .build();
    }

    public static ShoppingCartSummaryDto getDefaultCartSummaryDto() {
        return ShoppingCartSummaryDto.builder()
                .publicId(CART_PUBLIC_ID)
                .totalAmount(TOTAL_AMOUNT)
                .clientPublicId(CLIENT_PUBLIC_ID)
                .totalItems(1)
                .build();
    }
}