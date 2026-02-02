package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Override
    public List<OrderDTO> getOrdersByClient(UUID clientPublicId) {
        return List.of();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(UUID employeePublicId) {
        return List.of();
    }

    @Override
    public OrderDTO createFromShoppingCart(UUID clientPublicId) {
        return null;
    }
}