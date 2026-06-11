package com.ledgerflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

// Maps to the "payments" table — tracks money received against orders.
// A single order can have multiple payments (e.g., partial payment now, rest later).
// The system checks that total payments never exceed the order amount.
@Entity
@Table(name = "payments")
@Data @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_gen")
    @SequenceGenerator(name = "pay_gen", sequenceName = "seq_payment_id", allocationSize = 1)
    @Column(name = "payment_id")
    private Long paymentId;

    // FK to orders — which order is this payment for
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_date")
    private LocalDate paymentDate = LocalDate.now();

    // The actual amount paid — must be at least 0.01
    @NotNull @DecimalMin("0.01")
    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;

    // How they paid: CASH, CARD, ONLINE, or BANK_TRANSFER
    @NotBlank
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    // Payment status: COMPLETED, PENDING, FAILED, or REFUNDED
    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "COMPLETED";
}
