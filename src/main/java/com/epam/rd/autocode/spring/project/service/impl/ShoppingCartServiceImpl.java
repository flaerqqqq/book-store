package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartItemMapper;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import com.epam.rd.autocode.spring.project.model.ShoppingCartItem;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartItemRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final static int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final ShoppingCartMapper cartMapper;
    private final ShoppingCartItemMapper cartItemMapper;

    @Override
    @Transactional
    public ShoppingCartDto createCartForUser(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        User user = getUserOrThrow(userPublicId);

        if (user.getShoppingCart() != null) {
            throw new AlreadyExistException("Shopping Cart already exists for a User with publicId: %s".formatted(userPublicId));
        }

        ShoppingCart cart = ShoppingCart.builder()
                .user(user)
                .build();
        user.setShoppingCart(cart);

        ShoppingCart savedCart = userRepository.save(user).getShoppingCart();

        return cartMapper.entityToDto(savedCart);
    }

    @Override
    public ShoppingCartDto getCartByUser(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        User user = getUserOrThrow(userPublicId);
        ShoppingCart userCart = getCartOrThrow(user);

        return cartMapper.entityToDto(userCart);
    }

    @Override
    public Page<ShoppingCartItemDto> getCartItems(UUID userPublicId, Pageable pageable) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        ShoppingCart userCart = getCartOrThrow(userPublicId);

        Page<ShoppingCartItem> cartItemPage = cartItemRepository.findByCart_PublicId(userCart.getPublicId(), pageable);

        return cartItemPage.map(cartItemMapper::entityToDto);
    }

    @Override
    public ShoppingCartSummaryDto getCartSummary(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        ShoppingCart userCart = getCartOrThrow(userPublicId);

        return cartMapper.entityToSummaryDto(userCart);
    }

    @Override
    @Transactional
    public void emptyCart(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        User user = getUserOrThrow(userPublicId);
        ShoppingCart userCart = getCartOrThrow(user);

        userCart.getCartItems().clear();
        userCart.setTotalAmount(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public ShoppingCartDto addItemToCart(UUID userPublicId, UUID bookPublicId, Integer quantity) {
        Objects.requireNonNull(userPublicId, "User public Id must not be null");
        Objects.requireNonNull(bookPublicId, "Book public Id must not be null");

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        User user = getUserOrThrow(userPublicId);
        ShoppingCart userCart = getCartOrThrow(user);

        Book book = getBookOrThrow(bookPublicId);

        Optional<ShoppingCartItem> existingCartItemOpt = userCart.getCartItems().stream()
                .filter(item -> Objects.equals(book.getPublicId(), item.getBook().getPublicId()))
                .findFirst();

        existingCartItemOpt.ifPresentOrElse((item) -> {
            item.setQuantity(item.getQuantity() + quantity);
        }, () -> {
            ShoppingCartItem cartItem = ShoppingCartItem.builder()
                .cart(userCart)
                .book(book)
                .priceAtAdd(book.getPrice())
                .build();
            cartItem.setQuantity(quantity);
            userCart.getCartItems().add(cartItem);
        });

        userCart.recalculateTotalAmount();
        ShoppingCart updatedUserCart = userRepository.save(user).getShoppingCart();

        return cartMapper.entityToDto(updatedUserCart);
    }

    private User getUserOrThrow(UUID userPublicId) {
        return userRepository.findByPublicId(userPublicId).orElseThrow(() ->
                new NotFoundException(User.class, "publicId", userPublicId));
    }

    private Book getBookOrThrow(UUID bookPublicId) {
        return bookRepository.findByPublicId(bookPublicId).orElseThrow(() ->
                new NotFoundException(Book.class, "publicId", bookPublicId));
    }

    private ShoppingCart getCartOrThrow(User user) {
        if (user.getShoppingCart() == null) {
            throw new NotFoundException("Shopping Cart for is not found for a User with publicId: %s".formatted(user.getPublicId()));
        }

        return user.getShoppingCart();
    }

    private ShoppingCart getCartOrThrow(UUID userPublicId) {
        if (!userRepository.existsByPublicId(userPublicId)) {
            throw new NotFoundException(User.class, "publicId", userPublicId);
        }

        return cartRepository.findByUser_PublicId(userPublicId).orElseThrow(() ->
                new NotFoundException("Shopping Cart for is not found for a User with publicId: %s".formatted(userPublicId))
        );
    }
}