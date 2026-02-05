package com.epam.rd.autocode.spring.project.service;

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
import com.epam.rd.autocode.spring.project.security.CustomUserDetails;
import com.epam.rd.autocode.spring.project.service.fixture.ClientTestFixture;
import com.epam.rd.autocode.spring.project.service.fixture.EmployeeTestFixture;
import com.epam.rd.autocode.spring.project.service.fixture.OrderTestFixture;
import com.epam.rd.autocode.spring.project.service.fixture.ShoppingCartTestFixture;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private ShoppingCartService cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private UUID orderPublicId;
    private UUID clientPublicId;
    private UUID employeePublicId;
    private Order orderEntity;
    private OrderDTO orderDto;
    private OrderSummaryDto orderSummaryDto;
    private OrderItem orderItemEntity;
    private OrderItemDto orderItemDto;
    private Client clientEntity;
    private Employee employeeEntity;
    private ShoppingCart cartEntity;
    private ShoppingCartItem cartItemEntity;

    @BeforeEach
    public void setUpFixture() {
        orderPublicId = OrderTestFixture.ORDER_PUBLIC_ID;
        clientPublicId = ClientTestFixture.DEFAULT_PUBLIC_ID;
        employeePublicId = EmployeeTestFixture.DEFAULT_PUBLIC_ID;

        orderEntity = OrderTestFixture.getCreatedOrder();
        orderDto = OrderTestFixture.getCreatedOrderDto();
        orderSummaryDto =  OrderTestFixture.getCreatedOrderSummaryDto();
        orderItemEntity = OrderTestFixture.getDefaultOrderItem();
        orderItemDto = OrderTestFixture.getDefaultOrderItemDto();
        clientEntity = ClientTestFixture.getDefaultClient();
        employeeEntity = EmployeeTestFixture.getDefaultEmployee();
        cartEntity = ShoppingCartTestFixture.getDefaultCart();
        cartItemEntity = ShoppingCartTestFixture.getDefaultCartItem();

        orderEntity.setClient(clientEntity);
        orderEntity.addOrderItem(orderItemEntity);
        orderDto.getOrderItems().add(orderItemDto);

        cartEntity.getCartItems().add(cartItemEntity);
        cartItemEntity.setCart(cartEntity);

        clientEntity.setShoppingCart(cartEntity);
        cartEntity.setClient(clientEntity);
        clientEntity.setBalance(BigDecimal.valueOf(10));
    }

    private void givenClientExists() {
        when(clientRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));
    }

    private void givenEmployeeExists() {
        when(employeeRepository.findByPublicId(employeePublicId)).thenReturn(Optional.of(employeeEntity));
    }

    private void givenOrderExists() {
        when(orderRepository.findByPublicId(orderPublicId)).thenReturn(Optional.of(orderEntity));
    }

    private void givenOrderExistsWithLock() {
        when(orderRepository.findByPublicIdWithLock(orderPublicId)).thenReturn(Optional.of(orderEntity));
    }

    @Nested
    class CreateFromShoppingCartTests {

        @Test
        void createFromShoppingCart_ShouldCreate() {
            OrderRequestDto orderRequestDto = OrderRequestDto.builder()
                    .deliveryType(DeliveryType.PICKUP)
                    .build();

            givenClientExists();
            when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
            when(orderMapper.entityToDto(any(Order.class))).thenReturn(orderDto);

            orderService.createFromShoppingCart(clientPublicId, orderRequestDto);

            ArgumentCaptor<Order> orderArgCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderArgCaptor.capture());
            Order capturedOrder =orderArgCaptor.getValue();

            assertThat(capturedOrder).isNotNull();
            assertThat(capturedOrder.getClient().getPublicId()).isEqualTo(clientPublicId);
            assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(clientEntity.getBalance()).isEqualTo(BigDecimal.valueOf(9));

            verify(orderRepository, times(1)).save(any(Order.class));
            verify(cartService, times(1)).emptyCart(clientPublicId);
        }

        @Test
        void createFromShoppingCart_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> orderService.createFromShoppingCart(clientPublicId, new OrderRequestDto()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void createFromShoppingCart_ShouldThrowNotFound_WhenCartForClientNotFound() {
            clientEntity.setShoppingCart(null);
            givenClientExists();

            assertThatThrownBy(() -> orderService.createFromShoppingCart(clientPublicId, new OrderRequestDto()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void createFromShoppingCart_ShouldThrowEmptyCartOnEmptyCart() {
            cartEntity.getCartItems().clear();
            givenClientExists();

            assertThatThrownBy(() -> orderService.createFromShoppingCart(clientPublicId, new OrderRequestDto()))
                    .isInstanceOf(EmptyCartException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void createFromShoppingCart_ShouldThrowInsufficientFunds_WhenOrderTotalExceedsClientBalance() {
            OrderRequestDto orderRequest = OrderRequestDto.builder().deliveryType(DeliveryType.PICKUP).build();
            clientEntity.setBalance(BigDecimal.ZERO);
            givenClientExists();

            assertThatThrownBy(() -> orderService.createFromShoppingCart(clientPublicId, orderRequest))
                    .isInstanceOf(InsufficientFundsException.class);
        }
    }

    @Nested
    class GetOrderItemsTests {

        @Test
        void gerOrderItems_ShouldReturnCorrectPage_WhenPageableIsProvided() {
            Pageable pageable = Pageable.ofSize(1);
            Page<OrderItem> orderItemPage = new PageImpl<>(Collections.singletonList(orderItemEntity), pageable, 1);

            when(orderItemRepository.findAllByOrder_PublicId(orderPublicId, pageable)).thenReturn(orderItemPage);
            when(orderItemMapper.entityToDto(orderItemEntity)).thenReturn(orderItemDto);

            Page<OrderItemDto> actualOrderItemPage = orderService.getOrderItems(orderPublicId, pageable);

            assertThat(actualOrderItemPage).isNotNull();
            assertThat(actualOrderItemPage.getContent())
                    .hasSize(1)
                    .containsExactly(orderItemDto);
        }

        @Test
        void getOrderItems_ShouldReturnDefaultPage_WhenPageableIsNotProvided() {
            Page<OrderItem> page = Page.empty();

            when(orderItemRepository.findAllByOrder_PublicId(any(UUID.class), any(Pageable.class))).thenReturn(page);

            Page<OrderItemDto> actualOrderItemPage = orderService.getOrderItems(orderPublicId, null);

            assertThat(actualOrderItemPage).isNotNull();

            ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(orderItemRepository).findAllByOrder_PublicId(any(UUID.class), pageableArgCaptor.capture());
            Pageable capturedPageable = pageableArgCaptor.getValue();

            assertThat(capturedPageable).isNotNull()
                    .extracting(Pageable::getPageSize)
                    .isEqualTo(DEFAULT_PAGE_SIZE);
        }
    }

    @Nested
    class GetOrderSummaryTests {

        @Test
        void getOrderSummary_ShouldReturnCorrectDto() {
            givenOrderExists();
            when(orderMapper.entityToSummaryDto(orderEntity)).thenReturn(orderSummaryDto);

            OrderSummaryDto actualOrderSummaryDto = orderService.getOrderSummary(orderPublicId);

            assertThat(actualOrderSummaryDto).isNotNull()
                    .extracting(OrderSummaryDto::getPublicId)
                    .isEqualTo(orderPublicId);
        }

        @Test
        void getOrderSummary_ShouldThrowNotFound_WhenOrderIsNotFound() {
            assertThatThrownBy(() -> orderService.getOrderSummary(orderPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(orderPublicId.toString());
        }
    }

    @Nested
    class GetFilteredOrderSummariesTests {

        @Test
        void getFilteredOrderSummaries_ShouldReturnCorrectPage_WhenPageableIsProvided() {
            CustomUserDetails userDetails = new CustomUserDetails(clientEntity);
            Pageable pageable = Pageable.ofSize(1);
            Page<Order> orderPage = new PageImpl<>(Collections.singletonList(orderEntity), pageable, 1);

            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
            when(orderMapper.entityToSummaryDto(orderEntity)).thenReturn(orderSummaryDto);

            Page<OrderSummaryDto> actualOrderSummaryDtoPage = orderService.getFilteredOrderSummaries(null, pageable, userDetails);

            assertThat(actualOrderSummaryDtoPage).isNotNull()
                    .hasSize(1)
                    .containsExactly(orderSummaryDto);
        }

        @Test
        void getFilteredOrderSummaries_ShouldReturnDefaultPage_WhenPageableIsNotProvided() {
            CustomUserDetails userDetails = new CustomUserDetails(clientEntity);
            Page<OrderItem> page = Page.empty();

            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            Page<OrderSummaryDto> actualOrderSummaryDtoPage = orderService.getFilteredOrderSummaries(null, null, userDetails);

            assertThat(actualOrderSummaryDtoPage ).isNotNull();

            ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(orderRepository).findAll(any(Specification.class), pageableArgCaptor.capture());
            Pageable capturedPageable = pageableArgCaptor.getValue();

            assertThat(capturedPageable).isNotNull()
                    .extracting(Pageable::getPageSize)
                    .isEqualTo(DEFAULT_PAGE_SIZE);
        }

        @Test
        void getFilteredOrderSummaries_ShouldReturnClientPageOnClientRequest() {
            CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
            OrderFilterDto filterDto = new OrderFilterDto();
            SimpleGrantedAuthority clientRole = new SimpleGrantedAuthority("ROLE_CLIENT");
            Page<OrderItem> page = Page.empty();

            when(mockUserDetails.getPublicId()).thenReturn(clientPublicId);
            when(mockUserDetails.getAuthorities()).thenReturn((Collection)Collections.singleton(clientRole));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            orderService.getFilteredOrderSummaries(filterDto, null, mockUserDetails);

            assertThat(filterDto.getClientPublicId()).isEqualTo(clientPublicId.toString());
            assertThat(filterDto.getEmployeePublicId()).isNull();
        }

        @Test
        void getFilteredOrderSummaries_ShouldReturnGeneralPageOnEmployeeRequest() {
            CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
            OrderFilterDto filterDto = new OrderFilterDto();
            SimpleGrantedAuthority clientRole = new SimpleGrantedAuthority("ROLE_EMPLOYEE");
            Page<OrderItem> page = Page.empty();

            when(mockUserDetails.getPublicId()).thenReturn(clientPublicId);
            when(mockUserDetails.getAuthorities()).thenReturn((Collection)Collections.singleton(clientRole));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            orderService.getFilteredOrderSummaries(filterDto, null, mockUserDetails);

            assertThat(filterDto.getClientPublicId()).isNull();
        }
    }

    @Nested
    class ClaimOrderTests {

        @Test
        void claimOrder_ShouldBeClaimed_WhenItIsCreatedAndNotClaimed() {
            orderEntity.setEmployee(null);
            orderEntity.setStatus(OrderStatus.CREATED);

            givenOrderExistsWithLock();
            givenEmployeeExists();
            when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
            when(orderMapper.entityToSummaryDto(any(Order.class))).thenReturn(orderSummaryDto);

            OrderSummaryDto actualOrderSummaryDto = orderService.claimOrder(orderPublicId, employeePublicId);

            assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CLAIMED);
            assertThat(orderEntity.getEmployee()).isEqualTo(employeeEntity);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        void claimOrder_ShouldThrowNotFound_WhenOrderIsNotFound() {
            assertThatThrownBy(() -> orderService.claimOrder(orderPublicId, employeePublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(orderPublicId.toString());
        }

        @Test
        void claimOrder_ShouldThrowIllegalOrderState_WhenOrderIsAlreadyClaimed() {
            givenOrderExistsWithLock();
            orderEntity.setEmployee(employeeEntity);

            assertThatThrownBy(() -> orderService.claimOrder(orderPublicId, employeePublicId))
                    .isInstanceOf(IllegalOrderStateException.class);
        }

        @Test
        void claimOrder_ShouldThrowIllegalOrderState_WhenClaimingOrderWithOtherStatusThanCreated() {
            givenOrderExistsWithLock();
            orderEntity.setEmployee(null);
            orderEntity.setStatus(OrderStatus.CLAIMED);

            assertThatThrownBy(() -> orderService.claimOrder(orderPublicId, employeePublicId))
                    .isInstanceOf(IllegalOrderStateException.class);
        }

        @Test
        void claimOrder_ShouldThrowNotFound_WhenEmployeeIsNotFound() {
            givenOrderExistsWithLock();

            assertThatThrownBy(() -> orderService.claimOrder(orderPublicId, employeePublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(employeePublicId.toString());
        }
    }

    @Nested
    class UpdateStatusTests {

        @Test
        void updateStatus_ShouldUpdate_WhenOrderClaimedByEmployeeAndCorrectStatus() {
            orderEntity.setStatus(OrderStatus.CLAIMED);

            when(orderRepository.findClaimedOrderWithLock(orderPublicId, employeePublicId)).thenReturn(Optional.of(orderEntity));
            when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
            when(orderMapper.entityToSummaryDto(any(Order.class))).thenReturn(orderSummaryDto);

            orderService.updateStatus(orderPublicId, employeePublicId, OrderStatus.CONFIRMED);

            assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        void updateStatus_ShouldThrowIllegalOrderState_WhenUsedNotSupportedStatus() {
            assertThatThrownBy(() -> orderService.updateStatus(orderPublicId, employeePublicId, OrderStatus.CREATED))
                    .isInstanceOf(IllegalOrderStateException.class)
                    .hasMessageContaining(OrderStatus.CREATED.toString());
        }

        @Test
        void updateStatus_ShouldThrowNotFound_WhenClaimedOrderIsNotFound() {
            when(orderRepository.findClaimedOrderWithLock(orderPublicId, employeePublicId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateStatus(orderPublicId, employeePublicId, OrderStatus.CONFIRMED))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(orderPublicId.toString());
        }

        @Test
        void updateStatus_ShouldThrowIllegalOrderState_WhenTryingToUpdateCancelledOrder() {
            orderEntity.setStatus(OrderStatus.CANCELLED);

            when(orderRepository.findClaimedOrderWithLock(orderPublicId, employeePublicId)).thenReturn(Optional.of(orderEntity));

            assertThatThrownBy(() -> orderService.updateStatus(orderPublicId, employeePublicId, OrderStatus.CONFIRMED))
                    .isInstanceOf(IllegalOrderStateException.class);
        }
    }

    @Nested
    class CancelOrderTests {

        @Test
        void cancelOrder_ShouldCancelForClientWithStatusCreated() {
            orderEntity.setStatus(OrderStatus.CREATED);
            clientEntity.setBalance(BigDecimal.ZERO);

            givenOrderExistsWithLock();
            when(userRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));
            when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
            when(orderMapper.entityToSummaryDto(any(Order.class))).thenReturn(orderSummaryDto);

            orderService.cancelOrder(orderPublicId, clientPublicId, null);

            assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(orderEntity.getCancelledBy()).isEqualTo(clientEntity);
            assertThat(clientEntity.getBalance()).isGreaterThan(BigDecimal.ZERO);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        void canceOrder_ShouldCancelForEmployee() {
            orderEntity.setStatus(OrderStatus.CLAIMED);
            orderEntity.setEmployee(employeeEntity);
            clientEntity.setBalance(BigDecimal.ZERO);

            givenOrderExistsWithLock();
            when(userRepository.findByPublicId(employeePublicId)).thenReturn(Optional.of(employeeEntity));
            when(orderRepository.save(any(Order.class))).thenReturn(orderEntity);
            when(orderMapper.entityToSummaryDto(any(Order.class))).thenReturn(orderSummaryDto);

            orderService.cancelOrder(orderPublicId, employeePublicId, null);

            assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(orderEntity.getCancelledBy()).isEqualTo(employeeEntity);
            assertThat(clientEntity.getBalance()).isGreaterThan(BigDecimal.ZERO);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        void cancelOrder_ShouldThrowNotFound_WhenOrderIsNotFound() {
            assertThatThrownBy(() -> orderService.cancelOrder(orderPublicId, employeePublicId, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(orderPublicId.toString());
        }

        @Test
        void cancelOrder_ShouldThrowIllegalOrderState_WhenOrderHasNotCancellableStatus() {
            orderEntity.setStatus(OrderStatus.CANCELLED);

            givenOrderExistsWithLock();

            assertThatThrownBy(() -> orderService.cancelOrder(orderPublicId, employeePublicId, null))
                    .isInstanceOf(IllegalOrderStateException.class);
        }

        @Test
        void cancelOrder_ShouldThrowAccessDenied_WhenClientIsNotOwner() {
            orderEntity.setStatus(OrderStatus.CREATED);
            orderEntity.setClient(new Client());

            givenOrderExistsWithLock();
            when(userRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));

            assertThatThrownBy(() -> orderService.cancelOrder(orderPublicId, clientPublicId, null))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        void cancelOrder_ShouldThrowIllegalOrderStatusForClient_WhenOrderIsNotInCreatedState() {
            orderEntity.setStatus(OrderStatus.CONFIRMED);

            givenOrderExistsWithLock();
            when(userRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));

            assertThatThrownBy(() -> orderService.cancelOrder(orderPublicId, clientPublicId, null))
                    .isInstanceOf(IllegalOrderStateException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void cancelOrder_ShouldThrowIllegalOrderStatus_WhenOrderIsNotClaimedByEmployee() {
            orderEntity.setEmployee(new Employee());

            givenOrderExistsWithLock();
            when(userRepository.findByPublicId(employeePublicId)).thenReturn(Optional.of(employeeEntity));

            assertThatThrownBy(() -> orderService.cancelOrder(orderPublicId, employeePublicId, null))
                    .isInstanceOf(IllegalOrderStateException.class)
                    .hasMessageContaining(employeePublicId.toString());
        }
    }

    @Nested
    class IsClaimedByEmployeeTests {

        @Test
        void isClaimedByEmployee_ShouldReturnRepositoryResult() {
            when(orderRepository.isClaimedByEmployee(orderPublicId, employeePublicId)).thenReturn(true);

            boolean result = orderService.isClaimedByEmployee(orderPublicId, employeePublicId);

            assertThat(result).isTrue();
        }
    }

    @Nested
    class IsCreatedByClientTests {

        @Test
        void isClaimedByEmployee_ShouldThrowException_WhenIdsAreNull() {
            assertThatThrownBy(() -> orderService.isClaimedByEmployee(null, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class GetAvailableStatusesForOrderTests {

        @Test
        void getAvailableStatusesForOrder_ShouldReturnWithoutCurrentStatus() {
            when(orderRepository.isClaimedByEmployee(orderPublicId, employeePublicId)).thenReturn(true);
            givenOrderExists();

            List<OrderStatus> orderStatuses = orderService.getAvailableStatusesForOrder(orderPublicId, employeePublicId);

            assertThat(orderStatuses).isNotNull()
                    .doesNotContain(orderEntity.getStatus());
        }

        @Test
        void getAvailableStatusesForOrder_ShouldReturnListWithoutDeliveryStatuses_IfOrderStatusIsPickup() {
            orderEntity.setDeliveryType(DeliveryType.PICKUP);

            when(orderRepository.isClaimedByEmployee(orderPublicId, employeePublicId)).thenReturn(true);
            givenOrderExists();

            List<OrderStatus> orderStatuses = orderService.getAvailableStatusesForOrder(orderPublicId, employeePublicId);

            assertThat(orderStatuses).isNotNull()
                    .doesNotContain(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        }

        @Test
        void getAvailableStatusesForOrder_ShouldThrowIllegalOrderState_WhenOrderIsNotClaimedByEmployee() {
            assertThatThrownBy(() -> orderService.getAvailableStatusesForOrder(orderPublicId, employeePublicId))
                    .isInstanceOf(IllegalOrderStateException.class)
                    .hasMessageContaining(employeePublicId.toString());
        }
    }
}