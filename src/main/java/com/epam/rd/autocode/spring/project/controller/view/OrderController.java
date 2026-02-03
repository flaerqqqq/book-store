package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String getOrdersPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
                                @ModelAttribute("orderFilter") @Valid OrderFilterDto orderFilterDto,
                                BindingResult bindingResult,
                                Model model) {
        Page<OrderSummaryDto> orderSummaryPage = orderService.getFilteredOrderSummaries(orderFilterDto, pageable, userDetails);

        model.addAttribute("orderSummaryPage", orderSummaryPage);

        return "order/order-list";
    }

    @GetMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public String getCheckoutPage(@ModelAttribute("orderRequest") OrderRequestDto requestDto) {
        return "order/checkout";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
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
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String getOrderPage(@PathVariable("orderPublicId") UUID orderPublicId,
                               @PageableDefault(sort = "quantity") Pageable pageable,
                               Model model) {
        Page<OrderItemDto> orderItemPage = orderService.getOrderItems(orderPublicId, pageable);
        OrderSummaryDto orderSummary = orderService.getOrderSummary(orderPublicId);

        model.addAttribute("orderItemPage", orderItemPage);
        model.addAttribute("orderSummary", orderSummary);

        return "order/order-page";
    }

    @PostMapping("/{orderPublicId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public String cancelOrderByClient(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable("orderPublicId") UUID orderPublicId,
                                      @RequestParam("reason") String reason,
                                      RedirectAttributes redirectAttributes) {
        orderService.cancelOrder(orderPublicId, userDetails.getPublicId(), reason);

        redirectAttributes.addAttribute("message" , "Order was cancelled");
        return "redirect:/orders/" + orderPublicId;
    }

    @ModelAttribute("deliveryTypes")
    public DeliveryType[] getDeliveryTypes() {
        return DeliveryType.values();
    }

    @ModelAttribute("orderStatuses")
    public OrderStatus[] getOrderStatuses() {
        return OrderStatus.values();
    }
}