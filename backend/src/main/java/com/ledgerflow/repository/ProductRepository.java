package com.ledgerflow.repository;

import com.ledgerflow.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Data access for the "products" table.
// Has search filters, stock alerts, and a multi-table JOIN for finding best sellers.
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Look up a product by its unique SKU code (e.g., "SKU-ELEC-001")
    Optional<Product> findBySkuCode(String skuCode);

    // Dynamic search with optional filters — same pattern as CustomerRepository.
    // Supports filtering by: product name or SKU, category, and active status.
    // All filters are optional — if null, they're skipped.
    @Query("""
        SELECT p FROM Product p
        WHERE (:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(p.skuCode) LIKE LOWER(CONCAT('%',:search,'%')))
          AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
          AND (:active IS NULL OR p.isActive = :active)
        ORDER BY p.productName
        """)
    Page<Product> search(@Param("search") String search,
                         @Param("categoryId") Long categoryId,
                         @Param("active") String active,
                         Pageable pageable);

    // Find products running low on stock (used in dashboard "Low Stock Alert" widget).
    // Threshold is usually 10 — anything at or below that is flagged.
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = 'Y' ORDER BY p.stockQuantity ASC")
    List<Product> findLowStock(@Param("threshold") int threshold);

    // --- ADVANCED QUERY: Top Selling Products ---
    // This is a native SQL query that joins 3 tables together:
    //   products -> order_items -> orders
    //
    // How it works:
    // 1. JOIN order_items to see which products appear in orders
    // 2. JOIN orders to filter out cancelled orders
    // 3. GROUP BY product to aggregate all sales for each product
    // 4. SUM(oi.quantity) = total units sold across all orders
    // 5. ORDER BY total units sold (highest first)
    // 6. FETCH FIRST 10 = only return the top 10 best sellers
    @Query(value = """
        SELECT p.* FROM products p
        JOIN order_items oi ON p.product_id = oi.product_id
        JOIN orders o       ON oi.order_id  = o.order_id
        WHERE o.order_status != 'CANCELLED'
          AND p.is_active = 'Y'
        GROUP BY p.product_id, p.category_id, p.product_name, p.sku_code,
                 p.unit_price, p.stock_quantity, p.is_active,
                 p.created_at, p.updated_at, p.updated_by_user_id
        ORDER BY SUM(oi.quantity) DESC
        FETCH FIRST 10 ROWS ONLY
        """, nativeQuery = true)
    List<Product> findTopSelling();
}
