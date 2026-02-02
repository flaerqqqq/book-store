package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookService {

    Page<BookDTO> findBooks(Pageable pageable);

    Page<BookDTO> findFilteredBooks(BookFilterDto filter, Pageable pageable);

    BookDTO getBookByPublicId(UUID publicId);

    BookDTO updateBookByPublicId(UUID publicId, BookDTO book);

    void deleteBookByPublicId(UUID publicId);

    BookDTO addBook(BookDTO book);

    long getBooksCount();
}