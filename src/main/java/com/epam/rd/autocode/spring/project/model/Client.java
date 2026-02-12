package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client extends User {

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShoppingCart shoppingCart;

    public Client(String email, String password, String name, BigDecimal balance) {
        super(email, password, name);
        changeBalance(balance);
    }

    public void changeBalance(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be mull");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater or equal to zero");
        }

        this.balance = amount;
    }

    public void changeShoppingCart(ShoppingCart shoppingCart) {
        if (this.shoppingCart != null) {
            this.shoppingCart.setClient(null);
        }

        this.shoppingCart = shoppingCart;

        if (shoppingCart != null && shoppingCart.getClient() != this) {
            shoppingCart.setClient(this);
        }
    }
}