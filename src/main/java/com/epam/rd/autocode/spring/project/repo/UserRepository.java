package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);
}