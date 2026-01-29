package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByPublicId(UUID publicId);

    Long deleteByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

}