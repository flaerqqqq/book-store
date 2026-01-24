package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        pageable = Objects.requireNonNullElse(pageable, Pageable.ofSize(DEFAULT_PAGE_SIZE));

        return bookRepository.findAll(pageable).map(bookMapper::entityToDto);
    }

    @Override
    public BookDTO getBookByName(String name) {
        Objects.requireNonNull(name, "Name must not be null");

        return bookMapper.entityToDto(getBookByNameOrThrow(name));
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String name, BookDTO book) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(book, "Book data must not be null");

        Book existingBook = getBookByNameOrThrow(name);
        bookMapper.updateBookFromDto(book, existingBook);
        Book savedBook = bookRepository.save(existingBook);

        return bookMapper.entityToDto(savedBook);
    }

    @Override
    @Transactional
    public void deleteBookByName(String name) {
        Objects.requireNonNull(name, "Name must not be null");

        Long deletedCount = bookRepository.deleteByName(name);

        if (deletedCount == 0) {
            throw new NotFoundException(Book.class, "name", name);
        }
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO book) {
        Objects.requireNonNull(book, "Book data must not be null");

        if (bookRepository.existsByName(book.getName())) {
            throw new AlreadyExistException(Book.class, "name", book.getName());
        }

        Book bookEntity = bookMapper.dtoToEntity(book);
        Book savedBook = bookRepository.save(bookEntity);

        return bookMapper.entityToDto(savedBook);
    }

    private Book getBookByNameOrThrow(String name) {
        return bookRepository.findByName(name).orElseThrow(() ->
                new NotFoundException(Book.class, "name", name));
    }
}