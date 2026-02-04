package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartItemRepository;
import com.epam.rd.autocode.spring.project.service.fixture.BookTestFixture;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private ShoppingCartItemRepository cartItemRepository;
    @Mock
    private ShoppingCartService cartService;
    private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private BookServiceImpl bookService;

    private UUID bookPublicId;
    private String bookName;
    private Book bookEntity;
    private BookDTO bookDto;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @BeforeEach
    void setUpFixture() {
        bookPublicId = BookTestFixture.DEFAULT_PUBLIC_ID;
        bookName = BookTestFixture.DEFAULT_NAME;
        bookEntity = BookTestFixture.getDefaultBook();
        bookDto = BookTestFixture.getDefaultBookDto();
    }

    @BeforeEach
    void setUpService() {
        bookService = new BookServiceImpl(
                bookRepository,
                cartItemRepository,
                cartService,
                bookMapper
        );
    }

    @Test
    void getAllPages_ShouldReturnPage_WhenPageableProvided() {
        Pageable inputPageable = Pageable.ofSize(1);
        Page<Book> foundPage = new PageImpl<>(Collections.singletonList(bookEntity), inputPageable, 1);

        when(bookRepository.findAll(inputPageable)).thenReturn(foundPage);

        Page<BookDTO> actualBookPage = bookService.findBooks(inputPageable);

        assertThat(actualBookPage).isNotNull()
                .hasSize(1)
                .extracting(BookDTO::getPublicId)
                .containsExactly(bookPublicId);
    }

    @Test
    void getAllPages_ShouldReturnDefaultPage_WhenInputPageableIsNull() {
        Page<Book> foundPage = Page.empty();

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(foundPage);

        bookService.findBooks(null);

        ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository, times(1)).findAll(pageableArgCaptor.capture());
        Pageable actualPageable = pageableArgCaptor.getValue();

        assertThat(actualPageable).isNotNull()
                .extracting(Pageable::getPageSize)
                .isEqualTo(DEFAULT_PAGE_SIZE);
    }

    @Test
    void getBookByPublicId_ShouldReturnCorrectBook_WhenBookWithSuchPublicIdExists() {
        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.of(bookEntity));

        BookDTO actualBookDto = bookService.getBookByPublicId(bookPublicId);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getPublicId)
                .isEqualTo(bookPublicId);
    }

    @Test
    void getBookByPublicId_ShouldThrowNotFound_WhenBookWithSuchPublicIdDoesNotExist() {
        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookByPublicId(bookPublicId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(bookPublicId.toString());
    }

    @Test
    void getBookByPublicId_ShouldThrowNullPointer_WhenInputBookNameIsNull() {
        assertThatThrownBy(() -> bookService.getBookByPublicId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Public ID must not be null");
    }

    @Test
    void updateBookByPublicId_ShouldReturnUpdatedBook_WhenBookExists() {
        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.of(bookEntity));
        when(bookRepository.save(bookEntity)).thenReturn(bookEntity);

        BookDTO actualBookDto = bookService.updateBookByPublicId(bookPublicId, bookDto);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getPublicId)
                .isEqualTo(bookPublicId);

        verify(bookRepository).save(bookEntity);
    }

    @Test
    void updateBookByPublicId_ShouldThrowNotFound_WhenBookDoesNotExist() {
        when(bookRepository.findByPublicId(bookPublicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBookByPublicId(bookPublicId, bookDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(bookPublicId.toString());
    }

    @Test
    void updateBookByPublicId_ShouldThrowNullPointer_WhenInputPublicIdIsNull() {
        assertThatThrownBy(() -> bookService.updateBookByPublicId(null, bookDto))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Public ID must not be null");
    }

    @Test
    void updateBookByPublicId_ShouldThrowNullPointer_WhenInputDtoIsNull() {
        assertThatThrownBy(() -> bookService.updateBookByPublicId(bookPublicId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Book data must not be null");
    }

    @Test
    void deleteBookByPublicId_ShouldDelete_WhenBookExists() {
        Long deletedCount = 1L;

        when(bookRepository.deleteByPublicId(bookPublicId)).thenReturn(deletedCount);

        bookService.deleteBookByPublicId(bookPublicId);

        verify(cartService).syncCartsWithDeletedBook(bookPublicId);
        verify(cartItemRepository).deleteByBook_PublicId(bookPublicId);
        verify(bookRepository).deleteByPublicId(bookPublicId);
    }

    @Test
    void deleteBookByPublicId_ShouldThrowNotFound_WhenBookDoesNotExist() {
        Long deletedCount = 0L;

        when(bookRepository.deleteByPublicId(bookPublicId)).thenReturn(deletedCount);

        assertThatThrownBy(() -> bookService.deleteBookByPublicId(bookPublicId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(bookPublicId.toString());

        verify(cartService).syncCartsWithDeletedBook(bookPublicId);
        verify(cartItemRepository).deleteByBook_PublicId(bookPublicId);
        verify(bookRepository).deleteByPublicId(bookPublicId);
    }

    @Test
    void deleteBookByPublicId_ShouldThrowNullPointer_WhenInputNameIsNull() {
        assertThatThrownBy(() -> bookService.deleteBookByPublicId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Public ID must not be null");
    }

    @Test
    void addBook_ShouldReturnCorrectBookDto() {
        when(bookRepository.save(any(Book.class))).thenReturn(bookEntity);

        BookDTO actualBookDto = bookService.addBook(bookDto);

        ArgumentCaptor<Book> bookArgCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookArgCaptor.capture());
        Book capturedBook = bookArgCaptor.getValue();

        assertThat(capturedBook).isNotNull()
                .extracting(Book::getName)
                .isEqualTo(bookName);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getName)
                .isEqualTo(bookName);
    }

    @Test
    void addBook_ShouldThrowNullPointer_WhenInputDtoIsNull() {
        assertThatThrownBy(() -> bookService.addBook(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Book data must not be null");
    }

    @Test
    void getBooksCountByFilter_ShouldReturnCount_WhenFilterProvided() {
        BookFilterDto filterDto = new BookFilterDto();
        Long expectedCount = 1L;

        when(bookRepository.count(any(Specification.class))).thenReturn(expectedCount);

        Long actualCount = bookService.getBooksCountByFilter(filterDto);

        assertThat(actualCount).isEqualTo(expectedCount);
        verify(bookRepository).count(any(Specification.class));
    }

    @Test
    void getBooksCountByFilter_ShouldUserDefaultFilter_WhenFilterNotProvided() {
        Long expectedCount = 1L;

        when(bookRepository.count(any(Specification.class))).thenReturn(expectedCount);

        Long actualCount = bookService.getBooksCountByFilter(null);

        assertThat(actualCount).isEqualTo(expectedCount);
        verify(bookRepository).count(any(Specification.class));
    }

    @Test
    void findFilteredBooks_ShouldReturnCorrectPage_WhenFilterAndPageableProvided() {
        BookFilterDto filterDto = new BookFilterDto();
        Pageable pageable = Pageable.ofSize( 1);
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(bookEntity), pageable, 1);

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);

        Page<BookDTO> actualBookPage = bookService.findFilteredBooks(filterDto, pageable);

        assertThat(actualBookPage).isNotNull();
        assertThat(actualBookPage.getTotalElements()).isEqualTo(1L);
        assertThat(actualBookPage.getContent())
                .hasSize(1)
                .extracting(BookDTO::getPublicId)
                .containsExactly(bookPublicId);
    }
}