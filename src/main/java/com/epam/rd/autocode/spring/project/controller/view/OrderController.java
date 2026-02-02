package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderItemDto;
import com.epam.rd.autocode.spring.project.dto.OrderRequestDto;
import com.epam.rd.autocode.spring.project.dto.OrderSummaryDto;
import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String getOrdersPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model) {
        Page<OrderSummaryDto> orderSummaryPage = orderService.getOrderSummariesByClient(userDetails.getPublicId(), pageable);

        model.addAttribute("orderSummaryPage", orderSummaryPage);

        return "order/order-list";
    }

    @GetMapping("/checkout")
    public String getCheckoutPage(@ModelAttribute("orderRequest") OrderRequestDto requestDto) {
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @ModelAttribute("orderRequest") @Valid OrderRequestDto requestDto,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "order/checkout";
        }

        OrderDTO orderDto = orderService.createFromShoppingCart(userDetails.getPublicId(), requestDto);

        return "redirect:/orders/" + orderDto.getPublicId();
    }

    @GetMapping("/{orderPublicId}")
    public String getOrderPage(@PathVariable("orderPublicId") UUID orderPublicId,
                               @PageableDefault(sort = "quantity") Pageable pageable,
                               Model model) {
        Page<OrderItemDto> orderItemPage = orderService.getOrderItems(orderPublicId, pageable);
        OrderSummaryDto orderSummary = orderService.getOrderSummary(orderPublicId);

        model.addAttribute("orderItemPage", orderItemPage);
        model.addAttribute("orderSummary", orderSummary);

        return "order/order-page";
    }

    @ModelAttribute("deliveryTypes")
    public DeliveryType[] getDeliveryTypes() {
        return DeliveryType.values();
    }
}