package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    OrderDTO createFromShoppingCart(UUID clientPublicId, OrderRequestDto orderRequest);

    Page<OrderItemDto> getOrderItems(UUID orderPublicId, Pageable pageable);

    OrderSummaryDto getOrderSummary(UUID orderPublicId);

    Page<OrderSummaryDto> getFilteredOrderSummaries(OrderFilterDto filter, Pageable pageable, CustomUserDetails userDetails);

    OrderSummaryDto claimOrder(UUID orderPublicId, UUID employeePublicId);

    OrderSummaryDto updateStatus(UUID orderPublicId, UUID employeePublicId, OrderStatus status);

    OrderSummaryDto cancelOrder(UUID orderPublicId, UUID cancelledByPublicId, String reason);

    boolean isClaimedByEmployee(UUID orderPublicId, UUID employeePublicId);

    boolean isCreatedByClient(UUID orderPublicId, UUID clientPublicId);

    List<OrderStatus> getAvailableStatusesForOrder(UUID orderPublicId, UUID employeePublicId);
}