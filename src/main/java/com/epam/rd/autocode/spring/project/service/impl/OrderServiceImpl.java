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
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.repo.specification.OrderSpecifications;
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final Set<OrderStatus> PICKUP_FORBIDDEN_STATUSES = EnumSet.of(
            OrderStatus.SHIPPED, OrderStatus.DELIVERED
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShoppingCartService cartService;

    @Override
    @Transactional
    public OrderDTO createFromShoppingCart(UUID clientPublicId, OrderRequestDto orderRequest) {
        Objects.requireNonNull(orderRequest, "Order request data must not be null");
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

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
        Objects.requireNonNull(userDetails, "User details must not be null");
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
        Objects.requireNonNull(employeePublicId, "Employee public ID must not be null");

        Order order = getOrderWithLockOrThrow(orderPublicId);

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

    @Override
    @Transactional
    public OrderSummaryDto updateStatus(UUID orderPublicId, UUID employeePublicId, OrderStatus status) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(employeePublicId, "Employee public ID must not be null");

        if (status == OrderStatus.CREATED || status == OrderStatus.CLAIMED || status == OrderStatus.CANCELLED) {
            throw new IllegalOrderStateException(
                    "Status %s must be updated via its dedicated action method, not a general update".formatted(status)
            );
        }

        Order claimedOrder = orderRepository.findClaimedOrderWithLock(orderPublicId, employeePublicId).orElseThrow(() ->
                new NotFoundException("Order with publicId: %s is not found or not claimed by Employee with publicId: %s".formatted(orderPublicId, employeePublicId)));

        if (claimedOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalOrderStateException("Status of a cancelled Order cannot be updated");
        }

        claimedOrder.setStatus(status);

        Order savedOrder = orderRepository.save(claimedOrder);

        return orderMapper.entityToSummaryDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderSummaryDto cancelOrder(UUID orderPublicId, UUID cancelledByPublicId, String reason) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(cancelledByPublicId, "User public ID must not be null");

        Order order = getOrderWithLockOrThrow(orderPublicId);
        OrderStatus status = order.getStatus();

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalOrderStateException("Order already has status 'CANCELED' with publicId: %s".formatted(orderPublicId));
        }

        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED) {
            throw new IllegalOrderStateException("Order cannot be canceled with status '%s' and publicId: %s".formatted(status, orderPublicId));
        }

        User cancelledBy = userRepository.findByPublicId(cancelledByPublicId).orElseThrow(() ->
                new NotFoundException(User.class, "publicId", cancelledByPublicId));

        if (cancelledBy instanceof Client client) {
            if (!Objects.equals(client.getPublicId(), order.getClient().getPublicId())) {
                throw new AccessDeniedException("Only Client who created order or Employee who claimed can cancel it");
            }
            if (status != OrderStatus.CREATED) {
                throw new IllegalOrderStateException("Order with publicId: %s is already claimed and cannot be canceled by Client with publicId: %s".formatted(orderPublicId, cancelledByPublicId));
            }

        } else if (cancelledBy instanceof Employee employee) {
            if (order.getEmployee() == null
                    || !Objects.equals(employee.getPublicId(), order.getEmployee().getPublicId())) {
               throw new IllegalOrderStateException("Order with publicId: %s is not claimed by Employee with publicId: %s".formatted(orderPublicId, cancelledByPublicId));
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(cancelledBy);
        order.setReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        BigDecimal clientBalance = order.getClient().getBalance();
        clientBalance = clientBalance == null ? BigDecimal.ZERO : clientBalance;

        BigDecimal orderTotalAmount = order.getTotalAmount();
        orderTotalAmount = orderTotalAmount == null ? BigDecimal.ZERO : orderTotalAmount;

        order.getClient().setBalance(clientBalance.add(orderTotalAmount));

        Order savedOrder = orderRepository.save(order);

        return orderMapper.entityToSummaryDto(savedOrder);
    }

    @Override
    public boolean isClaimedByEmployee(UUID orderPublicId, UUID employeePublicId) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(employeePublicId, "Employee public ID must not be null");

        return orderRepository.isClaimedByEmployee(orderPublicId, employeePublicId);
    }

    @Override
    public boolean isCreatedByClient(UUID orderPublicId, UUID clientPublicId) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(clientPublicId, "Client public ID must not be null");

        return orderRepository.isCreatedByClient(orderPublicId, clientPublicId);
    }

    @Override
    public List<OrderStatus> getAvailableStatusesForOrder(UUID orderPublicId, UUID employeePublicId) {
        Objects.requireNonNull(orderPublicId, "Order public ID must not be null");
        Objects.requireNonNull(employeePublicId, "Employee public ID must not be null");

        if (!isClaimedByEmployee(orderPublicId, employeePublicId)) {
            throw new IllegalOrderStateException(
                    "Order with publicId: %s is not claimed by current Employee with publicId: %s"
                            .formatted(orderPublicId, employeePublicId)
            );
        }

        Order order = getOrderOrThrow(orderPublicId);

        List<OrderStatus> orderStatuses = new ArrayList<>(EnumSet.allOf(OrderStatus.class));

        if (order.getDeliveryType() == DeliveryType.PICKUP) {
            orderStatuses.removeAll(PICKUP_FORBIDDEN_STATUSES);
        }

        orderStatuses.remove(OrderStatus.CREATED);
        orderStatuses.remove(OrderStatus.CLAIMED);
        orderStatuses.remove(OrderStatus.CANCELLED);
        orderStatuses.remove(order.getStatus());

        return Collections.unmodifiableList(orderStatuses);
    }

    private Order getOrderWithLockOrThrow(UUID orderPublicId) {
        return orderRepository.findByPublicIdWithLock(orderPublicId).orElseThrow(() ->
                new NotFoundException(Order.class, "publicId", orderPublicId));
    }

    private Order getOrderOrThrow(UUID orderPublicId) {
        return orderRepository.findByPublicId(orderPublicId).orElseThrow(() ->
                new NotFoundException(Order.class, "publicId", orderPublicId));
    }

    private void checkBalanceAndSubtract(Client client, BigDecimal totalAmount) {
        if (client.getBalance().compareTo(totalAmount) <= 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds: Required %s, but Client has only %s"
                            .formatted(totalAmount, client.getBalance()),
                    client.getBalance(),
                    totalAmount
            );
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