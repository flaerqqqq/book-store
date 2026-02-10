package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_login_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginState {

    @Id
    private Long id;

    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "first_failed_at")
    private Instant firstFailedAt;

    @Column(name = "last_failed_at")
    private Instant lastFailedAt;

    @Column(name = "blocked_until")
    private Instant blockedUntil;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Version
    private Long version;

    public boolean isBlocked() {
        return blockedUntil != null && blockedUntil.isAfter(Instant.now());
    }

    public void reset() {
        failedAttempts = 0;
        firstFailedAt = null;
        lastFailedAt = null;
        blockedUntil = null;
    }
}