package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId = UUID.randomUUID();

    @Builder.Default
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartItem> cartItems = new ArrayList<>();

    public void recalculateTotalAmount() {
        this.totalAmount = cartItems.stream()
                .map(ShoppingCartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}