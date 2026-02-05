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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Creating shopping cart for client: {}", clientPublicId);

        Client client = getClientOrThrow(clientPublicId);

        if (client.getShoppingCart() != null) {
            log.warn("Cart creation failed: client {} already has a cart", clientPublicId);
            throw new AlreadyExistException("Shopping Cart already exists for a Client with publicId: %s".formatted(clientPublicId));
        }

        ShoppingCart cart = ShoppingCart.builder()
                .client(client)
                .build();
        client.setShoppingCart(cart);

        ShoppingCart savedCart = clientRepository.save(client).getShoppingCart();
        log.info("Cart created successfully for client: {}", clientPublicId);

        return cartMapper.entityToDto(savedCart);
    }

    @Override
    public ShoppingCartDto getCart(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");
        log.debug("Fetching cart for client: {}", clientPublicId);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        return cartMapper.entityToDto(cart);
    }

    @Override
    public Page<ShoppingCartItemDto> getCartItems(UUID clientPublicId, Pageable pageable) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");
        log.debug("Fetching items for client cart: {}", clientPublicId);
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        Page<ShoppingCartItem> cartItemPage = cartItemRepository.findByCart_PublicId(cart.getPublicId(), pageable);

        return cartItemPage.map(cartItemMapper::entityToDto);
    }

    @Override
    public ShoppingCartSummaryDto getCartSummary(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");
        log.debug("Fetching summary for client cart: {}", clientPublicId);

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        return cartMapper.entityToSummaryDto(cart);
    }

    @Override
    @Transactional
    public void emptyCart(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");
        log.info("Emptying cart for client: {}", clientPublicId);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        log.info("Cart emptied for client: {}", clientPublicId);
    }

    @Override
    @Transactional
    public ShoppingCartDto addItemToCart(UUID clientPublicId, UUID bookPublicId, Integer quantity) {
        validateInputs(clientPublicId, bookPublicId, quantity);
        log.info("Adding book {} (qty: {}) to cart for client {}", bookPublicId, quantity, clientPublicId);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        Book book = getBookOrThrow(bookPublicId);

        Optional<ShoppingCartItem> existingCartItemOpt = findItemInCart(cart, book.getPublicId());

        existingCartItemOpt.ifPresentOrElse((item) -> {
            log.debug("Incrementing quantity for book {} in cart {}", bookPublicId, clientPublicId);
            item.setQuantity(item.getQuantity() + quantity);
        }, () -> {
            log.debug("Adding new item for book {} to cart {}", bookPublicId, clientPublicId);
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
        log.info("Updating quantity for book {} to {} in client cart {}", bookPublicId, quantity, clientPublicId);

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        ShoppingCartItem existingCartItem = findItemInCart(cart, bookPublicId).orElseThrow(() -> {
            log.warn("Update failed: book {} not in cart {}", bookPublicId, clientPublicId);
            return new NotFoundException(ShoppingCartItem.class, "bookPublicId", bookPublicId);
        });

        existingCartItem.setQuantity(quantity);
        return saveAndMap(cart, client);
    }

    @Override
    @Transactional
    public void removeCartItem(UUID clientPublicId, UUID bookPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");
        log.info("Removing book {} from cart for client {}", bookPublicId, clientPublicId);

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        boolean removed = cart.getCartItems().removeIf(item ->
                Objects.equals(item.getBook().getPublicId(), bookPublicId));

        if (removed) {
            log.debug("Book {} removed, recalculating total", bookPublicId);
            cart.recalculateTotalAmount();
        } else {
            log.warn("Remove failed: book {} not found in cart {}", bookPublicId, clientPublicId);
            throw new NotFoundException(Book.class, "publicId", bookPublicId);
        }
    }

    @Override
    public Set<UUID> getCartItemBookIds(UUID clientPublicId) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");
        log.debug("Fetching book IDs in cart for client {}", clientPublicId);

        ShoppingCart cart = getCartOrThrow(clientPublicId);

        return cart.getCartItems().stream()
                .map(item -> item.getBook().getPublicId())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void syncCartsWithDeletedBook(UUID bookPublicId) {
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");
        log.info("Syncing all carts: removing deleted book {}", bookPublicId);

        List<ShoppingCart> carts = cartRepository.findAllByCartItems_Book_PublicId(bookPublicId);
        log.debug("Found {} carts containing book {}", carts.size(), bookPublicId);

        for (ShoppingCart cart : carts) {
            cart.getCartItems().removeIf(item -> Objects.equals(item.getBook().getPublicId(), bookPublicId));
            cart.recalculateTotalAmount();
        }
    }

    private ShoppingCartDto saveAndMap(ShoppingCart cart, Client client) {
        cart.recalculateTotalAmount();
        ShoppingCart updatedCart = clientRepository.save(client).getShoppingCart();
        log.debug("Cart total recalculated: {}", updatedCart.getTotalAmount());

        return cartMapper.entityToDto(updatedCart);
    }

    private void validateInputs(UUID clientPublicId, UUID bookPublicId, Integer quantity) {
        Objects.requireNonNull(clientPublicId, "Client public Id must not be null");
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");

        if (quantity == null || quantity <= 0) {
            log.warn("Validation failed: quantity {} is invalid", quantity);
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