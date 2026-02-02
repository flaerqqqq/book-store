package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto createCart(UUID clientPublicId);

    ShoppingCartDto getCart(UUID clientPublicId);

    Page<ShoppingCartItemDto> getCartItems(UUID clientPublicId, Pageable pageable);

    ShoppingCartSummaryDto getCartSummary(UUID clientPublicId);

    void emptyCart(UUID userPublicId);

    ShoppingCartDto addItemToCart(UUID clientPublicId, UUID bookPublicId, Integer quantity);

    ShoppingCartDto updateCartItemQuantity(UUID clientPublicId, UUID bookPublicId, Integer quantity);

    void removeCartItem(UUID clientPublicId, UUID bookPublicId);

    Set<UUID> getCartItemBookIds(UUID clientPublicId);

    void syncCartsWithDeletedBook(UUID bookPublicId);
}