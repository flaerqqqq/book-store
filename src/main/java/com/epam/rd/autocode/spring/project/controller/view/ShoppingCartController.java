package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService cartService;
    private final ShoppingCartMapper cartMapper;

    @GetMapping
    public String getCartPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {
        Page<ShoppingCartItemDto> cartItemPage = cartService.getCartItems(userDetails.getPublicId(), pageable);
        ShoppingCartSummaryDto summary = cartService.getCartSummary(userDetails.getPublicId());
        model.addAttribute("cartItemPage", cartItemPage);
        model.addAttribute("summary", summary);

        return "shopping-cart/items";
    }

}