package com.ledgerflow.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

// Maps to the "order_items" table — this is the junction/bridge table between Orders and Products.
// Think of it this way: if a customer buys a laptop and headphones in one purchase,
// that's 1 Order but 2 OrderItems.
// Each row stores: which order, which product, how many, and at what price.
@Entity
@Table(name = "order_items")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oi_gen")
    @SequenceGenerator(name = "oi_gen", sequenceName = "seq_order_item_id", allocationSize = 1)
    @Column(name = "order_item_id")
    private Long orderItemId;

    // FK to orders — which order does this item belong to
    // @JsonBackReference prevents infinite JSON loop with Order's @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    // FK to products — which product was purchased
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // How many units of this product were bought
    @NotNull @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // We snapshot the price at the time of purchase, so if the product price
    // changes later, old orders still show the correct amount the customer paid
    @NotNull @DecimalMin("0.01")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // Computed on-the-fly (not stored in DB) — just quantity * unit_price
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
