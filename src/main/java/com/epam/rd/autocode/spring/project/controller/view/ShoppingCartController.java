package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemRequestDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/shopping-cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    @GetMapping
    public String getCartPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PageableDefault(sort = "quantity") Pageable pageable,
                              Model model) {
        Page<ShoppingCartItemDto> cartItemPage = cartService.getCartItems(userDetails.getPublicId(), pageable);
        ShoppingCartSummaryDto summary = cartService.getCartSummary(userDetails.getPublicId());
        model.addAttribute("cartItemPage", cartItemPage);
        model.addAttribute("summary", summary);

        return "shopping-cart/items";
    }

    @PostMapping("/empty")
    public String emptyShoppingCart(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        cartService.emptyCart(customUserDetails.getPublicId());

        return "redirect:/";
    }

    @PostMapping("/remove-item")
    public String removeCartItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("bookPublicId") UUID bookPublicId,
                                 Pageable pageable,
                                 RedirectAttributes redirectAttributes) {
        addPaginationAttributes(pageable, redirectAttributes);

        cartService.removeCartItem(userDetails.getPublicId(), bookPublicId);

        int totalItems = cartService.getCartSummary(userDetails.getPublicId()).getTotalItems();
        if (totalItems == pageable.getPageSize() * (pageable.getPageNumber()) && pageable.getPageNumber() > 0) {
            redirectAttributes.addAttribute("page", pageable.getPageNumber() - 1);
        }

        return "redirect:/shopping-cart";
    }

    @PostMapping("/update-item")
    public String updateCartItemQuantity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @ModelAttribute @Valid ShoppingCartItemRequestDto requestDto,
                                         BindingResult bindingResult,
                                         Pageable pageable,
                                         RedirectAttributes redirectAttributes) {
        addPaginationAttributes(pageable, redirectAttributes);

        if (bindingResult.hasErrors()) {
            return "redirect:/shopping-cart?error=invalid_quantity";
        }

        cartService.updateCartItemQuantity(userDetails.getPublicId(), requestDto.getBookPublicId(), requestDto.getQuantity());

        return "redirect:/shopping-cart";
    }

    private void addPaginationAttributes(Pageable pageable, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("page", pageable.getPageNumber());
        redirectAttributes.addAttribute("size", pageable.getPageSize());
        if (pageable.getSort().isSorted()) {
            redirectAttributes.addAttribute("sort",
                    pageable.getSort().toString().replace(": ", ","));
        }
    }
}