package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ShoppingCartItemRepository;
import com.epam.rd.autocode.spring.project.repo.specification.BookSpecifications;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

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
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return bookRepository.findAll(pageable).map(bookMapper::entityToDto);
    }

    @Override
    public Page<BookDTO> findFilteredBooks(BookFilterDto filter, Pageable pageable) {
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

        return bookMapper.entityToDto(getBookByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public BookDTO updateBookByPublicId(UUID publicId, BookDTO book) {
        Objects.requireNonNull(publicId, "Public ID must not be null");
        Objects.requireNonNull(book, "Book data must not be null");

        Book existingBook = getBookByPublicIdOrThrow(publicId);
        bookMapper.updateBookFromDto(book, existingBook);
        Book savedBook = bookRepository.save(existingBook);

        return bookMapper.entityToDto(savedBook);
    }

    @Override
    @Transactional
    public void deleteBookByPublicId(UUID publicId) {
        Objects.requireNonNull(publicId, "Public ID must not be null");

        cartService.syncCartsWithDeletedBook(publicId);
        cartItemRepository.deleteByBook_PublicId(publicId);

        Long deletedCount = bookRepository.deleteByPublicId(publicId);

        if (deletedCount == 0) {
            throw new NotFoundException(Book.class, "publicId", publicId);
        }
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Objects.requireNonNull(book, "Book data must not be null");

        if (bookRepository.existsByPublicId(book.getPublicId())) {
            throw new AlreadyExistException(Book.class, "publicId", book.getPublicId());
        }

        Book bookEntity = bookMapper.dtoToEntity(book);
        Book savedBook = bookRepository.save(bookEntity);

        return bookMapper.entityToDto(savedBook);
    }

    @Override
    public long getBooksCount() {
        return bookRepository.count();
    }

    private Book getBookByPublicIdOrThrow(UUID publicId) {
        return bookRepository.findByPublicId(publicId).orElseThrow(() ->
                new NotFoundException(Book.class, "publicId", publicId));
    }
}