package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartItemMapper;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final static int DEFAULT_PAGE_SIZE = 10;

    private final ClientRepository clientRepository;
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final ShoppingCartMapper cartMapper;
    private final ShoppingCartItemMapper cartItemMapper;

    @Override
    @Transactional
    public ShoppingCartDto createCart(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

        Client client = getClientOrThrow(clientPublicId);

        if (client.getShoppingCart() != null) {
            throw new AlreadyExistException("Shopping Cart already exists for a Client with publicId: %s".formatted(clientPublicId));
        }

        ShoppingCart cart = ShoppingCart.builder()
                .client(client)
                .build();
        client.setShoppingCart(cart);

        ShoppingCart savedCart = clientRepository.save(client).getShoppingCart();

        return cartMapper.entityToDto(savedCart);
    }

    @Override
    public ShoppingCartDto getCart(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        return cartMapper.entityToDto(cart);
    }

    @Override
    public Page<ShoppingCartItemDto> getCartItems(UUID clientPublicId, Pageable pageable) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        Page<ShoppingCartItem> cartItemPage = cartItemRepository.findByCart_PublicId(cart.getPublicId(), pageable);

        return cartItemPage.map(cartItemMapper::entityToDto);
    }

    @Override
    public ShoppingCartSummaryDto getCartSummary(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        return cartMapper.entityToSummaryDto(cart);
    }

    @Override
    @Transactional
    public void emptyCart(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public ShoppingCartDto addItemToCart(UUID clientPublicId, UUID bookPublicId, Integer quantity) {
        validateInputs(clientPublicId, bookPublicId, quantity);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        Book book = getBookOrThrow(bookPublicId);

        Optional<ShoppingCartItem> existingCartItemOpt = findItemInCart(cart, book.getPublicId());

        existingCartItemOpt.ifPresentOrElse((item) -> {
            item.setQuantity(item.getQuantity() + quantity);
        }, () -> {
            ShoppingCartItem cartItem = ShoppingCartItem.builder()
                .cart(cart)
                .book(book)
                .priceAtAdd(book.getPrice())
                .build();
            cartItem.setQuantity(quantity);
            cart.getCartItems().add(cartItem);
        });

        return saveAndMap(cart, client);
    }

    @Override
    @Transactional
    public ShoppingCartDto updateCartItemQuantity(UUID clientPublicId, UUID bookPublicId, Integer quantity) {
        validateInputs(clientPublicId, bookPublicId, quantity);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        ShoppingCartItem existingCartItem = findItemInCart(cart, bookPublicId).orElseThrow(() ->
                        new NotFoundException(ShoppingCartItem.class, "bookPublicId", bookPublicId));

        existingCartItem.setQuantity(quantity);
        return saveAndMap(cart, client);
    }

    @Override
    @Transactional
    public void removeCartItem(UUID clientPublicId, UUID bookPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        boolean removed = cart.getCartItems().removeIf(item ->
                Objects.equals(item.getBook().getPublicId(), bookPublicId));

        if (removed) {
            cart.recalculateTotalAmount();
        } else {
            throw new NotFoundException(Book.class, "publicId", bookPublicId);
        }
    }

    @Override
    public Set<UUID> getCartItemBookIds(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        return cart.getCartItems().stream()
                .map(item -> item.getBook().getPublicId())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void syncCartsWithDeletedBook(UUID bookPublicId) {
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");

        List<ShoppingCart> carts = cartRepository.findAllByCartItems_Book_PublicId(bookPublicId);

        for (ShoppingCart cart : carts) {
            cart.getCartItems().removeIf(item -> Objects.equals(item.getBook().getPublicId(), bookPublicId));
            cart.recalculateTotalAmount();
        }
    }

    private ShoppingCartDto saveAndMap(ShoppingCart cart, Client client) {
        cart.recalculateTotalAmount();
        ShoppingCart updatedCart = clientRepository.save(client).getShoppingCart();

        return cartMapper.entityToDto(updatedCart);
    }

    private void validateInputs(UUID clientPublicId, UUID bookPublicId, Integer quantity) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private Optional<ShoppingCartItem> findItemInCart(ShoppingCart cart, UUID bookPublicId) {
        return cart.getCartItems().stream()
                .filter(item -> Objects.equals(bookPublicId, item.getBook().getPublicId()))
                .findFirst();
    }

    private Client getClientOrThrow(UUID clientPublicId) {
        return clientRepository.findByPublicId(clientPublicId).orElseThrow(() ->
                new NotFoundException(Client.class, "publicId", clientPublicId));
    }

    private Book getBookOrThrow(UUID bookPublicId) {
        return bookRepository.findByPublicId(bookPublicId).orElseThrow(() ->
                new NotFoundException(Book.class, "publicId", bookPublicId));
    }

    private ShoppingCart getCartOrThrow(Client client) {
        if (client.getShoppingCart() == null) {
            throw new NotFoundException("Shopping Cart for is not found for a Client with publicId: %s".formatted(client.getPublicId()));
        }

        return client.getShoppingCart();
    }

    private ShoppingCart getCartOrThrow(UUID clientPublicId) {
        if (!clientRepository.existsByPublicId(clientPublicId)) {
            throw new NotFoundException(Client.class, "publicId", clientPublicId);
        }

        return cartRepository.findByClient_PublicId(clientPublicId).orElseThrow(() ->
                new NotFoundException("Shopping Cart for is not found for a Client with publicId: %s".formatted(clientPublicId))
        );
    }
}