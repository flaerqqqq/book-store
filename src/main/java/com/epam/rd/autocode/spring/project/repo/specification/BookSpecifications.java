package com.epam.rd.autocode.spring.project.repo.specification;

import com.epam.rd.autocode.spring.project.dto.BookFilterDto;
import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecifications {

    private static final double GENRE_SIMILARITY_SCORE = 0.4;
    private static final double AUTHOR_SIMILARITY_SCORE = 0.2;

    public static Specification<Book> withFilters(BookFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(filter.getGenre())) {
                Expression<Double> similarityExp = cb.function("word_similarity", Double.class,
                        root.get("genre"), cb.literal(filter.getGenre()));
                predicates.add(cb.greaterThan(similarityExp, GENRE_SIMILARITY_SCORE));
            }

            if (filter.getAgeGroup() != null) {
                predicates.add(
                        cb.equal(root.get("ageGroup"), filter.getAgeGroup())
                );
            }

            if (filter.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice())
                );
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice())
                );
            }

            if (filter.getStartPublicationDate() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("publicationDate"), filter.getStartPublicationDate())
                );
            }

            if (filter.getEndPublicationDate() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("publicationDate"), filter.getEndPublicationDate())
                );
            }

            if (StringUtils.isNotBlank(filter.getAuthor())) {
                Expression<Double> similarityExp = cb.function("word_similarity", Double.class,
                        root.get("author"), cb.literal(filter.getAuthor()));

                predicates.add(cb.greaterThan(similarityExp, AUTHOR_SIMILARITY_SCORE));
            }

            if (filter.getLanguages() != null && !filter.getLanguages().isEmpty()) {
                predicates.add(
                        root.get("language").in(filter.getLanguages())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}