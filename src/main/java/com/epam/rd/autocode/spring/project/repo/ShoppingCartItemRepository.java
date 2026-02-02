package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ShoppingCartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {

    void deleteByBook_PublicId(UUID bookPublicId);

    Page<ShoppingCartItem> findByCart_PublicId(UUID cartPublicId, Pageable pageable);
}