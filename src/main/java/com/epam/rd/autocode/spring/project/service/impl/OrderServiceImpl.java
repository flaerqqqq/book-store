package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.mapper.OrderMapper;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final ClientRepository
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderDTO> getOrdersByClient(UUID clientPublicId, Pageable pageable) {
        Objects.requireNonNull(clientPublicId, "Client public ID mus not be null");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return orderRepository.findAllByClient_PublicId(clientPublicId, pageable)
                .map(orderMapper::entityToDto);
    }

    @Override
    public Page<OrderDTO> getOrdersByEmployee(UUID employeePublicId, Pageable pageable) {
        Objects.requireNonNull(employeePublicId, "Employee public ID mus not be null");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return orderRepository.findAllByEmployee_PublicId(employeePublicId, pageable)
                .map(orderMapper::entityToDto);
    }

    @Override
    public OrderDTO createFromShoppingCart(UUID clientPublicId) {
        return null;
    }
}