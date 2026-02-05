package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Objects.requireNonNull(username, "Username must not be null");

        log.debug("Loading user details for email: {}", username);

        return userRepository.findByEmail(username)
                .map(user -> {
                    log.debug("User found: {}. Mapping to CustomUserDetails.", username);
                    return new CustomUserDetails(user);
                })
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User with email {} not found", username);
                    return new UsernameNotFoundException("Can't find user with email: %s".formatted(username));
                });
    }
}