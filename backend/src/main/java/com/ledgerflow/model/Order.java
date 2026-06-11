package com.ledgerflow.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Maps to the "orders" table — the main sales invoice/receipt.
// An order is created when a customer buys something from our store.
// It links to: which customer bought it, and which staff member processed the sale.
@Entity
@Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor
public class Order {

    // Order IDs start from 1001 (configured in the sequence) to look more professional
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ord_gen")
    @SequenceGenerator(name = "ord_gen", sequenceName = "seq_order_id", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    // FK #1 — which customer placed this order
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // FK #2 — which staff member (cashier/admin) created this order
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(name = "order_date")
    private LocalDate orderDate = LocalDate.now();

    // Status follows a strict workflow: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
    // An order can also be CANCELLED at certain stages
    @Column(name = "order_status", length = 20)
    private String orderStatus = "PENDING";

    // Total price for all items in this order
    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Optional notes like "deliver before 5pm" or "fragile items"
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // One-to-Many: a single order can have multiple items (e.g., laptop + headphones)
    // CascadeType.ALL = when we save the order, its items get saved automatically
    // @JsonManagedReference prevents infinite JSON loops (Order -> Items -> Order -> Items...)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> orderItems = new ArrayList<>();
}
