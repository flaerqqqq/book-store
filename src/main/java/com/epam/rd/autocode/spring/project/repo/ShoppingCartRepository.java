package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    boolean existsByClient_PublicId(UUID clientPublicId);

    Optional<ShoppingCart> findByClient_PublicId(UUID clientPublicId);

    List<ShoppingCart> findAllByCartItems_Book_PublicId(UUID bookPublicId);
}