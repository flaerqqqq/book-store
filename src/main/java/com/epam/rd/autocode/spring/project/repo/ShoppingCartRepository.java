package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    boolean existsByUser_PublicId(UUID userPublicId);

    Optional<ShoppingCart> findByUser_PublicId(UUID userPublicId);
}