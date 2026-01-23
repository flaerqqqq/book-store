package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "publication_year", nullable = false)
    private LocalDate publicationDate;

    @Column(nullable = false)
    private String author;

    @Column(name = "number_of_pages", nullable = false)
    private Integer pages;

    @Column(nullable = false)
    private String characteristics;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private Language language;
}