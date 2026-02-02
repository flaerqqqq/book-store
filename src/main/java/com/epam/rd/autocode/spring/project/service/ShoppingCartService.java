package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto createCartForUser(UUID userPublicId);

    ShoppingCartDto getCartByUser(UUID userPublicId);

    Page<ShoppingCartItemDto> getCartItems(UUID userPublicId, Pageable pageable);

    ShoppingCartSummaryDto getCartSummary(UUID userPublicId);

    void emptyCart(UUID userPublicId);

    ShoppingCartDto addItemToCart(UUID userPublicId, UUID bookPublicId, Integer quantity);

    ShoppingCartDto updateCartItemQuantity(UUID userPublicId, UUID bookPublicId, Integer quantity);

    void removeCartItem(UUID userPublicId, UUID bookPublicId);

    Set<UUID> getCartItemBookIds(UUID userPublicId);

    void syncCartsWithDeletedBook(UUID bookPublicId);
}