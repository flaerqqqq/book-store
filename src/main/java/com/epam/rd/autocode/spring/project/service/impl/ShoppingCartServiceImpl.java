package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final UserRepository userRepository;
    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    @Transactional
    public ShoppingCartDto createCartForUser(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        User user = getUserOrThrow(userPublicId);

        if (user.getShoppingCart() != null) {
            throw new AlreadyExistException("Shopping Cart already exists for a User with publicId: %s".formatted(userPublicId));
        }

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .user(user)
                .build();
        user.setShoppingCart(shoppingCart);

        ShoppingCart savedShoppingCart = userRepository.save(user).getShoppingCart();

        return shoppingCartMapper.entityToDto(savedShoppingCart);
    }

    @Override
    @Transactional
    public void emptyCart(UUID userPublicId) {
        Objects.requireNonNull(userPublicId, "User public ID must not be null");

        User user = getUserOrThrow(userPublicId);

        if (user.getShoppingCart() == null) {
            throw new NotFoundException("Shopping Cart for is not found for a User with publicId: %s".formatted(userPublicId));
        }

        ShoppingCart shoppingCart = user.getShoppingCart();
        shoppingCart.getCartItems().clear();
        shoppingCart.setTotalAmount(BigDecimal.ZERO);
    }

    private User getUserOrThrow(UUID userPublicId) {
        return userRepository.findByPublicId(userPublicId).orElseThrow(() ->
                new NotFoundException(User.class, "publicId", userPublicId));
    }
}