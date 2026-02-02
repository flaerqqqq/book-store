package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderItemDto;
import com.epam.rd.autocode.spring.project.dto.OrderRequestDto;
import com.epam.rd.autocode.spring.project.dto.OrderSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    Page<OrderDTO> getOrdersByClient(UUID clientPublicId, Pageable pageable);

    Page<OrderDTO> getOrdersByEmployee(UUID employeePublicId, Pageable pageable);

    OrderDTO createFromShoppingCart(UUID clientPublicId, OrderRequestDto orderRequest);

    Page<OrderItemDto> getOrderItems(UUID orderPublicId, Pageable pageable);

    OrderSummaryDto getOrderSummary(UUID orderPublicId);

    Page<OrderSummaryDto> getOrderSummariesByClient(UUID clientPublicId, Pageable pageable);
}