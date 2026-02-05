package com.epam.rd.autocode.spring.project.controller.view;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
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

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ShoppingCartService cartService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String getOrdersPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
                                @ModelAttribute("orderFilter") @Valid OrderFilterDto orderFilterDto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("orderSummaryPage", Page.empty());
            return "order/order-list";
        }

        Page<OrderSummaryDto> orderSummaryPage = orderService.getFilteredOrderSummaries(orderFilterDto, pageable, userDetails);

        model.addAttribute("orderSummaryPage", orderSummaryPage);

        return "order/order-list";
    }

    @GetMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public String getCheckoutPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @ModelAttribute("orderRequest") OrderRequestDto requestDto,
                                  Model model) {
        addCartSummaryToModel(userDetails, model);

        return "order/checkout";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @ModelAttribute("orderRequest") @Valid OrderRequestDto requestDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCartSummaryToModel(userDetails, model);
            return "order/checkout";
        }

        OrderDTO orderDto;

        try  {
             orderDto = orderService.createFromShoppingCart(userDetails.getPublicId(), requestDto);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Insufficient funds, current balance: %s"
                            .formatted(e.getCurrentBalance())
            );
            return "redirect:/shopping-cart";
        }

        return "redirect:/orders/" + orderDto.getPublicId();
    }

    @GetMapping("/{orderPublicId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String getOrderPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("orderPublicId") UUID orderPublicId,
                               @PageableDefault(sort = "quantity") Pageable pageable,
                               Model model) {
        Page<OrderItemDto> orderItemPage = orderService.getOrderItems(orderPublicId, pageable);
        OrderSummaryDto orderSummary = orderService.getOrderSummary(orderPublicId);
        boolean isClaimedByEmployee = orderService.isClaimedByEmployee(orderPublicId, userDetails.getPublicId());
        boolean isCreatedByClient = orderService.isCreatedByClient(orderPublicId, userDetails.getPublicId());

        if (isClaimedByEmployee) {
            List<OrderStatus> availableOrderStatuses = orderService.getAvailableStatusesForOrder(orderPublicId, userDetails.getPublicId());
            model.addAttribute("availableOrderStatuses", availableOrderStatuses);
        }

        model.addAttribute("orderItemPage", orderItemPage);
        model.addAttribute("orderSummary", orderSummary);
        model.addAttribute("isClaimedByEmployee", isClaimedByEmployee);
        model.addAttribute("isCreatedByClient", isCreatedByClient);

        return "order/order-page";
    }

    @PostMapping("/{orderPublicId}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable("orderPublicId") UUID orderPublicId,
                                      @RequestParam("reason") String reason,
                                      RedirectAttributes redirectAttributes) {
        orderService.cancelOrder(orderPublicId, userDetails.getPublicId(), reason);

        redirectAttributes.addFlashAttribute("message" , "Order was cancelled");

        return "redirect:/orders/" + orderPublicId;
    }

    @PostMapping("/{orderPublicId}/claim")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String claimOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("orderPublicId") UUID orderPublicId,
                             RedirectAttributes redirectAttributes) {
        orderService.claimOrder(orderPublicId, userDetails.getPublicId());

        redirectAttributes.addAttribute("message", "Order was claimed");

        return "redirect:/orders/" + orderPublicId;
    }

    @PostMapping("/{orderPublicId}/update-status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateOrderStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable("orderPublicId") UUID orderPublicId,
                                    @RequestParam("orderStatus") OrderStatus orderStatus,
                                    RedirectAttributes redirectAttributes) {
        orderService.updateStatus(orderPublicId, userDetails.getPublicId(), orderStatus);

        redirectAttributes.addAttribute("message", "Order status was updated");

        return "redirect:/orders/" + orderPublicId;
    }

    private void addCartSummaryToModel(CustomUserDetails userDetails, Model model) {
        ShoppingCartSummaryDto cartSummary = cartService.getCartSummary(userDetails.getPublicId());
        model.addAttribute("cartSummary", cartSummary);
    }
}