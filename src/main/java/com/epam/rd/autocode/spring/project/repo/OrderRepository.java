package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByClient_PublicId(UUID clientPublicId, Pageable pageable);

    Page<Order> findAllByEmployee_PublicId(UUID employeePublicId, Pageable pageable);
}