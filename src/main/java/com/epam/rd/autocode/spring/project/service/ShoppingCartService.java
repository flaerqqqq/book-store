package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;

import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto createCartForUser(UUID userPublicId);

    ShoppingCartDto getCartByUser(UUID userPublicId);

    void emptyCart(UUID userPublicId);

    ShoppingCartDto addItemToCart(UUID userPublicId, UUID bookPublicId, Integer quantity);
}