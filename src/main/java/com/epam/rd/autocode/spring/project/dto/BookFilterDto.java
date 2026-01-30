package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.validation.annotation.ValidDateRange;
import com.epam.rd.autocode.spring.project.validation.annotation.ValidPriceRange;
import com.epam.rd.autocode.spring.project.validation.validator.DateRangeAware;
import com.epam.rd.autocode.spring.project.validation.validator.PriceRangeAware;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidPriceRange
@ValidDateRange
public class BookFilterDto implements PriceRangeAware, DateRangeAware {

    private String genre;

    private AgeGroup ageGroup;

    @Min(value = 0, message = "Price must not be negative")
    private BigDecimal minPrice;

    @Min(value = 0, message = "Price must not be negative")
    private BigDecimal maxPrice;

    private LocalDate startPublicationDate;

    private LocalDate endPublicationDate;

    private String author;

    private List<Language> languages;


    @Override
    public LocalDate getDateFrom() {
        return startPublicationDate;
    }

    @Override
    public LocalDate getDateTo() {
        return endPublicationDate;
    }
}