package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByEmail(String email);

    Long deleteByEmail(String email);

    Optional<Client> findByEmail(String email);
}