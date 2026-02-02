package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.EmptyCartException;
import com.epam.rd.autocode.spring.project.exception.IllegalOrderStateException;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.OrderItemMapper;
import com.epam.rd.autocode.spring.project.mapper.OrderMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.DeliveryType;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderItemRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.repo.specification.OrderSpecifications;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShoppingCartService cartService;

    @Override
    @Transactional
    public OrderDTO createFromShoppingCart(UUID clientPublicId, OrderRequestDto orderRequest) {
        Objects.requireNonNull(clientPublicId, "Client public ID mus not be null");

        Client client = getClientOrThrow(clientPublicId);
        ShoppingCart cart = getCartOrThrow(client);

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyCartException("Cannot create an Order for Client with publicId: %s because the Shopping Cart is empty".formatted(clientPublicId));
        }

        List<OrderItem> orderItems = mapCartToOrderItems(cart.getCartItems());

        Order order = Order.builder()
                .client(client)
                .deliveryType(orderRequest.getDeliveryType())
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .comment(orderRequest.getComment())
                .status(OrderStatus.CREATED)
                .build();
        orderItems.forEach(order::addOrderItem);

        if (orderRequest.getDeliveryType() == DeliveryType.PICKUP) {
            order.setDeliveryAddress(null);
        }

        order.recalculateTotalAmount(orderRequest.getDeliveryType().getBaseCost());
        checkBalanceAndSubtract(client, order.getTotalAmount());

        Order savedOrder = orderRepository.save(order);

        cartService.emptyCart(clientPublicId);

        return orderMapper.entityToDto(savedOrder);
    }

    @Override
    public Page<OrderItemDto> getOrderItems(UUID orderPublicId, Pageable pageable) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return orderItemRepository.findAllByOrder_PublicId(orderPublicId, pageable)
                .map(orderItemMapper::entityToDto);
    }

    @Override
    public OrderSummaryDto getOrderSummary(UUID orderPublicId) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");

        return orderMapper.entityToSummaryDto(getOrderOrThrow(orderPublicId));
    }

    @Override
    public Page<OrderSummaryDto> getFilteredOrderSummaries(OrderFilterDto filter, Pageable pageable, CustomUserDetails userDetails) {
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));
        filter = Objects.requireNonNullElse(filter, new OrderFilterDto());

        UUID callerPublicId = userDetails.getPublicId();
        boolean isClient = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENT"));

        if (isClient) {
            filter.setClientPublicId(callerPublicId.toString());
            filter.setEmployeePublicId(null);
        }

        Specification<Order> orderSpecification = OrderSpecifications.withFilters(filter);

        return orderRepository.findAll(orderSpecification, pageable)
                .map(orderMapper::entityToSummaryDto);
    }

    @Override
    @Transactional
    public OrderSummaryDto claimOrder(UUID orderPublicId, UUID employeePublicId) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(employeePublicId, "Order public ID must not be null");

        Order order = orderRepository.findByPublicIdWithLock(orderPublicId).orElseThrow(() ->
                new NotFoundException(Order.class, "publicId", orderPublicId));

        if (order.getEmployee() != null) {
            throw new IllegalOrderStateException("Order is already claimed by another Employee");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalOrderStateException("Only Orders with status: CREATED can be claimed");
        }

        Employee employee = getEmployeeOrThrow(employeePublicId);
        order.setEmployee(employee);
        order.setStatus(OrderStatus.CLAIMED);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.entityToSummaryDto(savedOrder);
    }

    private Order getOrderOrThrow(UUID orderPublicId) {
        return orderRepository.findByPublicId(orderPublicId).orElseThrow(() ->
                new NotFoundException(Order.class, "publicId", orderPublicId));
    }

    private void checkBalanceAndSubtract(Client client, BigDecimal totalAmount) {
        if (client.getBalance().compareTo(totalAmount) <= 0) {
            throw new InsufficientFundsException("Insufficient funds: Required %s, but Client has only %s".formatted(totalAmount, client.getBalance()));
        }
        client.setBalance(client.getBalance().subtract(totalAmount));
    }

    private List<OrderItem> mapCartToOrderItems(List<ShoppingCartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .bookPublicId(cartItem.getBook().getPublicId())
                        .bookName(cartItem.getBook().getName())
                        .priceAtPurchase(cartItem.getPriceAtAdd())
                        .quantity(cartItem.getQuantity())
                        .subtotal(cartItem.getSubtotal())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private ShoppingCart getCartOrThrow(Client client) {
        if (client.getShoppingCart() == null) {
            throw new NotFoundException("Shopping Cart for is not found for a Client with publicId: %s".formatted(client.getPublicId()));
        }

        return client.getShoppingCart();
    }

    private Client getClientOrThrow(UUID clientPublicId) {
        return clientRepository.findByPublicId(clientPublicId).orElseThrow(() ->
                new NotFoundException(Client.class, "publicId", clientPublicId));
    }

    private Employee getEmployeeOrThrow(UUID employeePublicId) {
        return employeeRepository.findByPublicId(employeePublicId).orElseThrow(() ->
                new NotFoundException(Employee.class, "publicId", employeePublicId));
    }
}