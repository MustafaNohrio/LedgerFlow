package com.ledgerflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Maps to the "products" table — the actual items we sell in the store.
// Each product belongs to a category and has a unique SKU code for identification.
@Entity
@Table(name = "products")
@Data @NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prod_gen")
    @SequenceGenerator(name = "prod_gen", sequenceName = "seq_product_id", allocationSize = 1)
    @Column(name = "product_id")
    private Long productId;

    // FK to categories — every product must sit inside a category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;

    @NotBlank @Size(max = 200)
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    // SKU = Stock Keeping Unit — a unique code like "SKU-ELEC-001"
    // Used for scanning and quick product lookup
    @NotBlank @Size(max = 50)
    @Column(name = "sku_code", nullable = false, unique = true, length = 50)
    private String skuCode;

    // Price must be at least 0.01 — we use BigDecimal for accurate money calculations
    @NotNull @DecimalMin("0.01")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // How many units we currently have in the warehouse
    // Can't go below 0 (enforced by a CHECK constraint in the DB)
    @Min(0)
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    // Soft-delete flag: "Y" means available for sale, "N" means discontinued
    // We don't actually delete products — we just mark them inactive
    // columnDefinition must match the Oracle schema exactly: CHAR(1)
    @Column(name = "is_active", columnDefinition = "CHAR(1)")
    private String isActive = "Y";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Tracks which staff member last modified this product (for audit purposes)
    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;
}
