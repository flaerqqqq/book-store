package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
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

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(
                bookRepository,
                bookMapper
        );
    }

    @Test
    void getAllPages_ShouldReturnPage_WhenPageableProvided() {
        Pageable inputPageable = Pageable.ofSize(1);
        Book foundBookInPage = BookTestFixture.getDefaultBook();
        Page<Book> foundPage = new PageImpl<>(Collections.singletonList(foundBookInPage), inputPageable, 1);

        when(bookRepository.findAll(inputPageable)).thenReturn(foundPage);

        Page<BookDTO> actualBookPage = bookService.getAllBooks(inputPageable);

        assertThat(actualBookPage).isNotNull()
                .hasSize(1)
                .extracting(BookDTO::getName)
                .containsExactly(foundBookInPage.getName());
    }

    @Test
    void getAllPages_ShouldReturnDefaultPage_WhenInputPageableIsNull() {
        int defaultPageSize = 10;
        Pageable inputPageable = null;
        Page<Book> foundPage = new PageImpl<>(Collections.emptyList());

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(foundPage);

        bookService.getAllBooks(inputPageable);

        ArgumentCaptor<Pageable> pageableArgCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository, times(1)).findAll(pageableArgCaptor.capture());
        Pageable actualPageable = pageableArgCaptor.getValue();

        assertThat(actualPageable).isNotNull()
                .extracting(Pageable::getPageSize)
                .isEqualTo(defaultPageSize);
    }

    @Test
    void getBookByName_ShouldReturnCorrectBook_WhenBookWithSuchNameExists() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        Book foundBook = BookTestFixture.getDefaultBook();

        when(bookRepository.findByName(inputBookName)).thenReturn(Optional.of(foundBook));

        BookDTO actualBookDto = bookService.getBookByName(inputBookName);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getName)
                .isEqualTo(inputBookName);
    }

    @Test
    void getBookByName_ShouldThrowNotFound_WhenBookWithSuchNameDoesNotExist() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;

        when(bookRepository.findByName(inputBookName)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> bookService.getBookByName(inputBookName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(inputBookName);
    }

    @Test
    void getBookByName_ShouldThrowNullPointer_WhenInputBookNameIsNull() {
        String inputBookName = null;

        assertThatThrownBy(() -> bookService.getBookByName(inputBookName))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Name must not be null");
    }

    @Test
    void updateBookByName_ShouldReturnUpdatedBook_WhenBookExists() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        BookDTO inputDto = BookTestFixture.getDefaultBookDto();
        Book foundBook = BookTestFixture.getDefaultBook();

        when(bookRepository.findByName(inputBookName)).thenReturn(Optional.of(foundBook));
        when(bookRepository.save(foundBook)).thenReturn(foundBook);

        BookDTO actualBookDto = bookService.updateBookByName(inputBookName, inputDto);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getName)
                .isEqualTo(inputBookName);

        verify(bookRepository).save(foundBook);
    }

    @Test
    void updateBookByName_ShouldThrowNotFound_WhenBookDoesNotExist() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        BookDTO inputDto = BookDTO.builder().build();

        when(bookRepository.findByName(inputBookName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBookByName(inputBookName, inputDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(inputBookName);
    }

    @Test
    void updateBookByName_ShouldThrowNullPointer_WhenInputNameIsNull() {
        String inputBookName = null;
        BookDTO inputDto = BookDTO.builder().build();

        assertThatThrownBy(() -> bookService.updateBookByName(inputBookName, inputDto))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name must not be null");
    }

    @Test
    void updateBookByName_ShouldThrowNullPointer_WhenInputDtoIsNull() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        BookDTO inputDto = null;

        assertThatThrownBy(() -> bookService.updateBookByName(inputBookName, inputDto))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Book data must not be null");
    }

    @Test
    void deleteBookByName_ShouldDelete_WhenBookExists() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        Long deletedCount = 1L;

        when(bookRepository.deleteByName(inputBookName)).thenReturn(deletedCount);

        bookService.deleteBookByName(inputBookName);

        verify(bookRepository).deleteByName(inputBookName);
    }

    @Test
    void deleteBookByName_ShouldThrowNotFound_WhenBookDoesNotExist() {
        String inputBookName = BookTestFixture.DEFAULT_NAME;
        Long deletedCount = 0L;

        when(bookRepository.deleteByName(inputBookName)).thenReturn(deletedCount);

        assertThatThrownBy(() -> bookService.deleteBookByName(inputBookName))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(inputBookName);

        verify(bookRepository).deleteByName(inputBookName);
    }

    @Test
    void deleteBookByName_ShouldThrowNullPointer_WhenInputNameIsNull() {
        String inputBookName = null;

        assertThatThrownBy(() -> bookService.deleteBookByName(inputBookName))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name must not be null");
    }

    @Test
    void addBook_ShouldReturnCorrectBookDto_WhenBookNameIsUnique() {
        BookDTO inputDto = BookTestFixture.getDefaultBookDto();
        String inputDtoName = inputDto.getName();
        Book mappedBookEntity = BookTestFixture.getDefaultBook();


        when(bookRepository.existsByName(inputDtoName)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(mappedBookEntity);

        BookDTO actualBookDto = bookService.addBook(inputDto);

        ArgumentCaptor<Book> bookArgCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookArgCaptor.capture());
        Book capturedBook = bookArgCaptor.getValue();

        assertThat(capturedBook).isNotNull()
                .extracting(Book::getName)
                .isEqualTo(inputDtoName);

        assertThat(actualBookDto).isNotNull()
                .extracting(BookDTO::getName)
                .isEqualTo(inputDtoName);
    }

    @Test
    void addBook_ShouldThrowAlreadyExistsOnBookNameDuplicate() {
        String inputDtoName = BookTestFixture.DEFAULT_NAME;
        BookDTO inputDto = BookDTO.builder().name(inputDtoName).build();

        when(bookRepository.existsByName(inputDtoName)).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(inputDto))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessageContaining(inputDtoName);
    }

    @Test
    void addBook_ShouldThrowNullPointer_WhenInputDtoIsNull() {
        BookDTO inputDto = null;

        assertThatThrownBy(() -> bookService.addBook(inputDto))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Book data must not be null");
    }
}