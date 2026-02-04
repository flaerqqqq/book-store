package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ShoppingCartDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartItemDto;
import com.epam.rd.autocode.spring.project.dto.ShoppingCartSummaryDto;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartItemMapper;
import com.epam.rd.autocode.spring.project.mapper.ShoppingCartMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import com.epam.rd.autocode.spring.project.model.ShoppingCartItem;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartItemRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartRepository;
import com.epam.rd.autocode.spring.project.service.fixture.ClientTestFixture;
import com.epam.rd.autocode.spring.project.service.fixture.ShoppingCartTestFixture;
import com.epam.rd.autocode.spring.project.service.impl.ShoppingCartServiceImpl;
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

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private ShoppingCartItemRepository cartItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ShoppingCartMapper cartMapper;
    @Mock
    private ShoppingCartItemMapper cartItemMapper;
    @InjectMocks
    private ShoppingCartServiceImpl cartService;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private UUID cartPublicId;
    private UUID cartItemPublicId;
    private UUID clientPublicId;
    private UUID bookPublicId;
    private ShoppingCart cartEntity;
    private ShoppingCartItem cartItemEntity;
    private ShoppingCartDto cartDto;
    private ShoppingCartSummaryDto cartSummaryDto;
    private ShoppingCartItemDto cartItemDto;
    private Book bookEntity;
    private Client clientEntity;

    @BeforeEach
    public void setUpFixture() {
        cartPublicId = ShoppingCartTestFixture.CART_PUBLIC_ID;
        cartItemPublicId = ShoppingCartTestFixture.CART_ITEM_PUBLIC_ID;
        clientPublicId = ShoppingCartTestFixture.CLIENT_PUBLIC_ID;

        cartEntity = ShoppingCartTestFixture.getDefaultCart();
        cartItemEntity = ShoppingCartTestFixture.getDefaultCartItem();
        cartDto = ShoppingCartTestFixture.getDefaultCartDto();
        cartSummaryDto = ShoppingCartTestFixture.getDefaultCartSummaryDto();
        cartItemDto = ShoppingCartTestFixture.getDefaultCartItemDto();

        clientEntity = ClientTestFixture.getDefaultClient();
        clientEntity.setPublicId(clientPublicId);

        cartEntity.setClient(clientEntity);
        clientEntity.setShoppingCart(cartEntity);
        cartEntity.getCartItems().add(cartItemEntity);
        cartItemEntity.setCart(cartEntity);

        bookEntity = cartItemEntity.getBook();
        bookPublicId = bookEntity.getPublicId();
    }

    private void givenClientExists() {
        when(clientRepository.findByPublicId(clientPublicId)).thenReturn(Optional.of(clientEntity));
    }

    private void givenClientDoesNotExist() {
        when(clientRepository.findByPublicId(clientPublicId)).thenReturn(Optional.empty());
    }

    private void givenCartExistsForClient() {
        when(cartRepository.findByClient_PublicId(clientPublicId)).thenReturn(Optional.of(cartEntity));
    }

    private void givenClientExists_checkOnly() {
        when(clientRepository.existsByPublicId(clientPublicId)).thenReturn(true);
    }

    @Nested
    class CreateCartTests {

        @Test
        void createCart_ShouldCreateCartSuccessfully_WhenNoCartExists() {
            clientEntity.setShoppingCart(null);

            givenClientExists();
            when(clientRepository.save(clientEntity)).thenReturn(clientEntity);
            when(cartMapper.entityToDto(any(ShoppingCart.class))).thenReturn(cartDto);

            ShoppingCartDto actualCartDto = cartService.createCart(clientPublicId);

            assertThat(actualCartDto.getClientPublicId()).isEqualTo(clientPublicId);

            verify(clientRepository, times(1)).save(clientEntity);
        }

        @Test
        void createCart_ShouldThrowAlreadyExists_WhenCartExistsForClient() {
            givenClientExists();

            assertThatThrownBy(() -> cartService.createCart(clientPublicId))
                    .isInstanceOf(AlreadyExistException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void createCart_ShouldThrowNotFound_WhenClientNotFound() {
            givenClientDoesNotExist();

            assertThatThrownBy(() -> cartService.createCart(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }
    }

    @Nested
    class GetCartTests {

        @Test
        void getCart_ShouldReturnCartForClient() {
            givenClientExists();
            when(cartMapper.entityToDto(cartEntity)).thenReturn(cartDto);

            ShoppingCartDto actualCartDto = cartService.getCart(clientPublicId);

            assertThat(actualCartDto).isNotNull()
                    .extracting(ShoppingCartDto::getClientPublicId)
                    .isEqualTo(clientPublicId);
        }

        @Test
        void getCart_ShouldThrowNotFound_WhenClientNotFound() {
            givenClientDoesNotExist();

            assertThatThrownBy(() -> cartService.getCart(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void getCart_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            clientEntity.setShoppingCart(null);

            givenClientExists();

            assertThatThrownBy(() -> cartService.getCart(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }
    }

    @Nested
    class GetCartItemsTests {

        @Test
        void getCartItems_ShouldReturnCartItemsPage_WhenCartByClientExists() {
            Pageable pageable = Pageable.ofSize(1);
            Page<ShoppingCartItem> cartItemPage = new PageImpl<>(Collections.singletonList(cartItemEntity), pageable, 1);

            givenClientExists_checkOnly();
            givenCartExistsForClient();
            when(cartItemRepository.findByCart_PublicId(cartPublicId, pageable)).thenReturn(cartItemPage);
            when(cartItemMapper.entityToDto(any(ShoppingCartItem.class))).thenReturn(cartItemDto);

            Page<ShoppingCartItemDto> actualCartItemDtoPage = cartService.getCartItems(clientPublicId, pageable);

            assertThat(actualCartItemDtoPage).isNotNull();
            assertThat(actualCartItemDtoPage.getPageable().getPageSize()).isEqualTo(1);
            assertThat(actualCartItemDtoPage.getContent())
                    .extracting(ShoppingCartItemDto::getPublicId)
                    .contains(cartItemPublicId);
        }

        @Test
        void getCartItems_ShouldReturnDefaultPage_WhenPageableNotProvided() {
            Page<ShoppingCartItem> cartItemPage = Page.empty();

            givenClientExists_checkOnly();
            givenCartExistsForClient();
            when(cartItemRepository.findByCart_PublicId(any(UUID.class), any(Pageable.class))).thenReturn(cartItemPage);

            Page<ShoppingCartItemDto> actualCartItemDtoPage = cartService.getCartItems(clientPublicId, null);

            ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(cartItemRepository).findByCart_PublicId(any(UUID.class), pageableArgCaptor.capture());

            Pageable capturedPageable = pageableArgCaptor.getValue();

            assertThat(capturedPageable).isNotNull();
            assertThat(capturedPageable.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);

            assertThat(actualCartItemDtoPage).isNotNull();
        }

        @Test
        void getCartItems_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.getCartItems(clientPublicId, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void getCartItems_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            clientEntity.setShoppingCart(null);

            assertThatThrownBy(() -> cartService.getCartItems(clientPublicId, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }
    }

    @Nested
    class GetCartSummaryTests {

        @Test
        void getCartSummary_ShouldReturnCartSummaryForClient() {
            givenClientExists_checkOnly();
            givenCartExistsForClient();
            when(cartMapper.entityToSummaryDto(cartEntity)).thenReturn(cartSummaryDto);

            ShoppingCartSummaryDto actualCartSummaryDto = cartService.getCartSummary(clientPublicId);

            assertThat(actualCartSummaryDto).isNotNull()
                    .extracting(ShoppingCartSummaryDto::getClientPublicId)
                    .isEqualTo(clientPublicId);
        }

        @Test
        void getCartSummary_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.getCartSummary(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void getCartSummary_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists_checkOnly();

            assertThatThrownBy(() -> cartService.getCartSummary(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }
    }

    @Nested
    class EmptyCartTests {

        @Test
        void emptyCart_ShouldEmptyWhenCart() {
            givenClientExists();

            cartService.emptyCart(clientPublicId);

            assertThat(cartEntity.getClient()).isEqualTo(clientEntity);
            assertThat(cartEntity.getCartItems()).hasSize(0);
            assertThat(cartEntity.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void emptyCart_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.emptyCart(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void emptyCart_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists();
            clientEntity.setShoppingCart(null);

            assertThatThrownBy(() -> cartService.emptyCart(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }
    }

    @Nested
    class AddItemToCartTests {

        @Test
        void addItemToCart_ShouldUpdateQuantity_WhenCartItemWithBookPresent() {
            givenClientExists();
            when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.of(bookEntity));
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);
            when(cartMapper.entityToDto(any(ShoppingCart.class))).thenReturn(cartDto);

            cartService.addItemToCart(clientPublicId, bookPublicId, 1);

            assertThat(cartEntity.getCartItems()).hasSize(1);
            assertThat(cartEntity.getTotalAmount()).isEqualTo(BigDecimal.valueOf(2));
            assertThat(cartItemEntity.getQuantity()).isEqualTo(2);
        }

        @Test
        void addItemToCart_ShouldAddNewCartItemForNewBook() {
            Book anotherBook = Book.builder()
                    .publicId(UUID.randomUUID())
                    .price(BigDecimal.ONE)
                    .build();
            givenClientExists();
            when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.of(anotherBook));
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);
            when(cartMapper.entityToDto(any(ShoppingCart.class))).thenReturn(cartDto);

            cartService.addItemToCart(clientPublicId, bookPublicId, 1);

            assertThat(cartEntity.getCartItems()).hasSize(2);
            assertThat(cartEntity.getTotalAmount()).isEqualTo(BigDecimal.valueOf(2));
            assertThat(cartEntity.getCartItems())
                    .extracting(item -> item.getBook().getPublicId())
                    .contains(anotherBook.getPublicId());
        }

        @Test
        void addItemToCart_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.addItemToCart(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void addItemToCart_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists();
            clientEntity.setShoppingCart(null);

            assertThatThrownBy(() -> cartService.addItemToCart(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void addItemToCart_ShouldThrowNotFound_WhenBookNotFound() {
            givenClientExists();

            assertThatThrownBy(() -> cartService.addItemToCart(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(bookPublicId.toString());
        }
    }

    @Nested
    class UpdateCartItemQuantityTests {

        @Test
        void updateCartItemQuantity_ShouldUpdate_WhenCartItemIsFoundByBook() {
            givenClientExists();
            when(clientRepository.save(any(Client.class))).thenReturn(clientEntity);
            when(cartMapper.entityToDto(any(ShoppingCart.class))).thenReturn(cartDto);

            cartService.updateCartItemQuantity(clientPublicId, bookPublicId, 2);

            assertThat(cartItemEntity.getQuantity()).isEqualTo(2);
        }

        @Test
        void updateCartItemQuantity_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.updateCartItemQuantity(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void updateCartItemQuantity_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists();
            clientEntity.setShoppingCart(null);

            assertThatThrownBy(() -> cartService.updateCartItemQuantity(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void updateCartItemQuantity_ShouldThrowNotFound_WhenCartItemForBookNotFound() {
            givenClientExists();
            cartEntity.getCartItems().clear();

            assertThatThrownBy(() -> cartService.updateCartItemQuantity(clientPublicId, bookPublicId, 1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(bookPublicId.toString());
        }
    }

    @Nested
    class RemoveCartItemTests {

        @Test
        void removeCartItem_ShouldRemove_IfCartItemFoundForBook() {
            givenClientExists_checkOnly();
            givenCartExistsForClient();

            cartService.removeCartItem(clientPublicId, bookPublicId);

            assertThat(cartEntity.getCartItems()).hasSize(0);
            assertThat(cartEntity.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void removeCartItem_ShouldThrowNotFound_IfCartItemNotFoundForBook() {
            givenClientExists_checkOnly();
            givenCartExistsForClient();
            cartEntity.getCartItems().clear();

            assertThatThrownBy(() -> cartService.removeCartItem(clientPublicId, bookPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(bookPublicId.toString());
        }

        @Test
        void removeCartItem_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.removeCartItem(clientPublicId, bookPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void removeCartItem_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists_checkOnly();

            assertThatThrownBy(() -> cartService.removeCartItem(clientPublicId, bookPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
            verify(cartRepository).findByClient_PublicId(clientPublicId);
        }
    }

    @Nested
    class GetCartItemBookIdsTests {

        @Test
        void getCartItemBookIds_ShouldReturnBookIdsForCart() {
            givenClientExists_checkOnly();
            givenCartExistsForClient();

            Set<UUID> actualBookIds = cartService.getCartItemBookIds(clientPublicId);

            assertThat(actualBookIds).isNotNull()
                    .hasSize(1)
                    .containsExactly(bookPublicId);
        }

        @Test
        void getCartItemBookIds_ShouldThrowNotFound_WhenClientNotFound() {
            assertThatThrownBy(() -> cartService.getCartItemBookIds(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
        }

        @Test
        void getCartItemBookIds_ShouldThrowNotFound_WhenCartNotFoundForClient() {
            givenClientExists_checkOnly();

            assertThatThrownBy(() -> cartService.getCartItemBookIds(clientPublicId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(clientPublicId.toString());
            verify(cartRepository).findByClient_PublicId(clientPublicId);
        }
    }

    @Nested
    class SyncCartsWithDeletedBookTests {

        @Test
        void syncCartsWithDeletedBook_ShouldRemoveCartItemsByBook() {
            List<ShoppingCart> carts = Collections.singletonList(cartEntity);

            when(cartRepository.findAllByCartItems_Book_PublicId(bookPublicId)).thenReturn(carts);

            cartService.syncCartsWithDeletedBook(bookPublicId);

            assertThat(cartEntity.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(cartEntity.getCartItems()).hasSize(0);
        }
    }
}