package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartItemRepository;
import com.epam.rd.autocode.spring.project.repo.specification.BookSpecifications;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookRepository bookRepository;
    private final ShoppingCartItemRepository cartItemRepository;
    private final ShoppingCartService cartService;
    private final BookMapper bookMapper;

    @Override
    public Page<BookDTO> findBooks(Pageable pageable) {
        log.debug("Fetching books page");
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return bookRepository.findAll(pageable).map(bookMapper::entityToDto);
    }

    @Override
    public Page<BookDTO> findFilteredBooks(BookFilterDto filter, Pageable pageable) {
        log.debug("Fetching filtered books with filters: {}", filter);
        if (filter == null) {
            return findBooks(pageable);
        }

        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));
        Specification<Book> bookSpec = BookSpecifications.withFilters(filter);

        return bookRepository.findAll(bookSpec, pageable)
                .map(bookMapper::entityToDto);
    }

    @Override
    public BookDTO getBookByPublicId(UUID publicId) {
        Objects.requireNonNull(publicId, "Public ID must not be null");
        log.debug("Fetching book by public ID: {}", publicId);

        return bookMapper.entityToDto(getBookByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public BookDTO updateBookByPublicId(UUID publicId, BookDTO book) {
        Objects.requireNonNull(publicId, "Public ID must not be null");
        Objects.requireNonNull(book, "Book data must not be null");

        log.info("Updating book with public ID: {}", publicId);

        Book existingBook = getBookByPublicIdOrThrow(publicId);
        bookMapper.updateBookFromDto(book, existingBook);
        Book savedBook = bookRepository.save(existingBook);

        return bookMapper.entityToDto(savedBook);
    }

    @Override
    @Transactional
    public void deleteBookByPublicId(UUID publicId) {
        Objects.requireNonNull(publicId, "Public ID must not be null");
        log.info("Deleting book with public ID: {}", publicId);

        cartService.syncCartsWithDeletedBook(publicId);
        cartItemRepository.deleteByBook_PublicId(publicId);

        Long deletedCount = bookRepository.deleteByPublicId(publicId);

        if (deletedCount == 0) {
            log.warn("Failed to delete book: ID {} not found", publicId);
            throw new NotFoundException(Book.class, "publicId", publicId);
        }
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Objects.requireNonNull(book, "Book data must not be null");
        log.info("Adding new book: {}", book.getName());

        Book bookEntity = bookMapper.dtoToEntity(book);
        Book savedBook = bookRepository.save(bookEntity);

        log.info("Book successfully added with public ID: {}", savedBook.getPublicId());
        return bookMapper.entityToDto(savedBook);
    }

    @Override
    public long getBooksCountByFilter(BookFilterDto bookFilter) {
        bookFilter = Objects.requireNonNullElse(bookFilter, new BookFilterDto());
        log.debug("Counting books with filters: {}", bookFilter);

        Specification<Book> bookSpecs = BookSpecifications.withFilters(bookFilter);

        return bookRepository.count(bookSpecs);
    }

    private Book getBookByPublicIdOrThrow(UUID publicId) {
        return bookRepository.findByPublicId(publicId).orElseThrow(() -> {
            log.warn("Book not found for ID: {}", publicId);
            return new NotFoundException(Book.class, "publicId", publicId);
        });
    }
}