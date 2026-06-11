package com.ledgerflow.repository;

import com.ledgerflow.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Data access for the "orders" table — the most query-heavy repository in the project.
// Contains the core analytical queries that power the Dashboard charts and Reports page.
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders for a specific customer, newest first
    List<Order> findByCustomerCustomerIdOrderByOrderDateDesc(Long customerId);

    // Dynamic search query with optional filters for the Orders list page.
    // All four parameters are optional — if null, they're ignored.
    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.orderStatus = :status)
          AND (:customerId IS NULL OR o.customer.customerId = :customerId)
          AND (:from IS NULL OR o.orderDate >= :from)
          AND (:to IS NULL OR o.orderDate <= :to)
        ORDER BY o.orderDate DESC
        """)
    Page<Order> search(@Param("status") String status,
                       @Param("customerId") Long customerId,
                       @Param("from") LocalDate from,
                       @Param("to") LocalDate to,
                       Pageable pageable);

    // Simple aggregation: total revenue from all delivered orders
    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    // Revenue for the current month only — used in the dashboard "Revenue This Month" card
    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount),0) FROM orders o
        WHERE o.order_status = 'DELIVERED'
          AND EXTRACT(MONTH FROM o.order_date) = EXTRACT(MONTH FROM SYSDATE)
          AND EXTRACT(YEAR FROM o.order_date) = EXTRACT(YEAR FROM SYSDATE)
        """, nativeQuery = true)
    BigDecimal getRevenueThisMonth();

    // Count orders grouped by status — feeds the "Orders by Status" panel on dashboard
    // Returns rows like: ["PENDING", 5], ["DELIVERED", 12], etc.
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> countByStatus();

    // Quick count of pending orders for the dashboard stat card
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'PENDING'")
    long countPending();

    // --- ADVANCED QUERY: Monthly Sales Chart Data ---
    // This powers the bar chart on the dashboard. Groups orders by month for the current year.
    //
    // How it works:
    // 1. EXTRACT(MONTH FROM order_date) pulls just the month number (1-12)
    // 2. CASE converts month numbers to labels ("Jan", "Feb", etc.) for the chart
    // 3. We only look at the current year and exclude cancelled orders
    // 4. COUNT(*) = how many orders that month, SUM(total_amount) = total revenue
    // 5. GROUP BY month, ORDER BY month number so Jan comes before Feb
    @Query(value = """
        SELECT CASE EXTRACT(MONTH FROM order_date)
                   WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar'
                   WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun'
                   WHEN 7 THEN 'Jul' WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep'
                   WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' WHEN 12 THEN 'Dec'
               END AS month_label,
               EXTRACT(MONTH FROM order_date) AS month_num,
               COUNT(*)                   AS order_count,
               SUM(total_amount)          AS revenue
        FROM orders
        WHERE EXTRACT(YEAR FROM order_date) = EXTRACT(YEAR FROM SYSDATE)
          AND order_status != 'CANCELLED'
        GROUP BY EXTRACT(MONTH FROM order_date)
        ORDER BY month_num
        """, nativeQuery = true)
    List<Object[]> getMonthlySales();

    // --- ADVANCED QUERY: Top Selling Products Report ---
    // Joins 4 tables: products + categories + order_items + orders
    // Calculates total units sold and total revenue per product
    // Only shows the top 20 best-selling products
    @Query(value = """
        SELECT p.product_name,
               p.sku_code,
               c.category_name,
               SUM(oi.quantity)                 AS total_units,
               SUM(oi.quantity * oi.unit_price)  AS total_revenue
        FROM products p
        JOIN categories c ON p.category_id = c.category_id
        JOIN order_items oi ON p.product_id = oi.product_id
        JOIN orders o ON oi.order_id = o.order_id
        WHERE o.order_status != 'CANCELLED'
        GROUP BY p.product_name, p.sku_code, c.category_name
        ORDER BY total_units DESC
        FETCH FIRST 20 ROWS ONLY
        """, nativeQuery = true)
    List<Object[]> getTopSellingProductsReport();

    // --- Low Stock Report ---
    // Finds all active products with 10 or fewer units in stock
    // Used in the Reports page and dashboard low-stock alert
    @Query(value = """
        SELECT p.product_name, p.sku_code, c.category_name,
               p.stock_quantity, p.unit_price
        FROM products p
        JOIN categories c ON p.category_id = c.category_id
        WHERE p.stock_quantity <= 10 AND p.is_active = 'Y'
        ORDER BY p.stock_quantity ASC
        """, nativeQuery = true)
    List<Object[]> getLowStockReport();

    // --- Customer Purchase History Report ---
    // Groups orders by customer to show their total spending and last purchase date
    // Uses INNER JOIN (not LEFT JOIN), so only customers who actually bought something appear
    @Query(value = """
        SELECT c.first_name || ' ' || c.last_name AS customer_name,
               c.city_name,
               COUNT(o.order_id)       AS total_orders,
               SUM(o.total_amount)     AS total_spent,
               MAX(o.order_date)       AS last_order_date
        FROM customers c
        JOIN orders o ON c.customer_id = o.customer_id
        WHERE o.order_status != 'CANCELLED'
        GROUP BY c.customer_id, c.first_name, c.last_name, c.city_name
        ORDER BY total_spent DESC
        """, nativeQuery = true)
    List<Object[]> getCustomerPurchaseHistory();

    // --- Payment Method Summary ---
    // Shows breakdown of how customers are paying (Cash vs Card vs Online etc.)
    // Useful for business decisions like "should we push online payments?"
    @Query(value = """
        SELECT p.payment_method,
               COUNT(*)          AS txn_count,
               SUM(p.amount_paid) AS total_amount
        FROM payments p
        WHERE p.payment_status = 'COMPLETED'
        GROUP BY p.payment_method
        ORDER BY total_amount DESC
        """, nativeQuery = true)
    List<Object[]> getPaymentMethodSummary();
}
