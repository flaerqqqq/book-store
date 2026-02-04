package com.epam.rd.autocode.spring.project.service.fixture;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BookTestFixture {

    public static final String DEFAULT_NAME = "book";
    public static final UUID DEFAULT_PUBLIC_ID = UUID.randomUUID();

    public static Book getDefaultBook() {
        return Book.builder()
                .id(1L)
                .publicId(DEFAULT_PUBLIC_ID)
                .name(DEFAULT_NAME)
                .genre("genre")
                .ageGroup(AgeGroup.ADULT)
                .price(BigDecimal.ONE)
                .publicationDate(LocalDate.now())
                .author("author")
                .pages(1)
                .characteristics("characteristics")
                .description("description")
                .language(Language.ENGLISH)
                .build();
    }

    public static BookDTO getDefaultBookDto() {
        return BookDTO.builder()
                .publicId(DEFAULT_PUBLIC_ID)
                .name(DEFAULT_NAME)
                .genre("genre")
                .ageGroup(AgeGroup.ADULT)
                .price(BigDecimal.ONE)
                .publicationDate(LocalDate.now())
                .author("author")
                .pages(1)
                .characteristics("characteristics")
                .description("description")
                .language(Language.ENGLISH)
                .build();
    }
}