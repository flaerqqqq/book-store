package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Page<OrderItem> findAllByOrder_PublicId(UUID orderPublicId, Pageable pageable);
}