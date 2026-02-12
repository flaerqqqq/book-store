package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    public User(String email, String password, String name) {
        changeEmail(email);
        changePassword(password);
        changeName(name);
    }

    public void changeEmail(String email) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
    }

    public void changePassword(String password) {
        this.password = Objects.requireNonNull(password, "Password must not be null");
    }

    public void changeName(String name) {
        this.name = Objects.requireNonNull(name, "Name must not be null");
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void removeRole(Role role) {
        this.roles.remove(Objects.requireNonNull(role, "Role must not be null"));
    }

    public void addRole(Role role) {
        this.roles.add(Objects.requireNonNull(role, "Role must not be null"));
    }
}