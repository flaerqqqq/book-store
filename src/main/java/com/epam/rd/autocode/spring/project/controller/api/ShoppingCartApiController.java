package com.epam.rd.autocode.spring.project.controller.api;

import com.epam.rd.autocode.spring.project.dto.AddItemToCartRequestDto;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopping-carts")
@RequiredArgsConstructor
public class ShoppingCartApiController {

    private final ShoppingCartService cartService;

    @PostMapping("/items")
    public void addItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @RequestBody @Valid AddItemToCartRequestDto requestDto) {
        cartService.addItemToCart(userDetails.getPublicId(), requestDto.getBookPublicId(), 1);
    }
}