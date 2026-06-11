package com.ledgerflow.repository;

import com.ledgerflow.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Data access for the "customers" table.
// Contains both simple lookups and a complex analytical query for the reports page.
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Used to check for duplicate email during customer creation
    Optional<Customer> findByEmailAddress(String emailAddress);

    // Dynamic search with optional filters.
    // If 'search' is null, it's ignored. If 'city' is null, it's ignored.
    // This lets us reuse one query for: "show all", "search by name", and "filter by city".
    // LOWER() + LIKE + CONCAT makes the search case-insensitive.
    @Query("""
        SELECT c FROM Customer c
        WHERE (:search IS NULL OR
               LOWER(c.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR
               LOWER(c.lastName)  LIKE LOWER(CONCAT('%',:search,'%')))
          AND (:city IS NULL OR LOWER(c.cityName) LIKE LOWER(CONCAT('%',:city,'%')))
        ORDER BY c.firstName, c.lastName
        """)
    Page<Customer> search(@Param("search") String search,
                          @Param("city") String city,
                          Pageable pageable);

    // --- ADVANCED QUERY: Customer Purchase Summary (used in Reports page) ---
    // This is a native SQL query that joins customers with their orders to rank them.
    //
    // How it works:
    // 1. LEFT JOIN ensures customers with zero orders still show up (they just get 0)
    // 2. COUNT(DISTINCT o.order_id) = how many separate orders this customer placed
    // 3. COALESCE(SUM(...), 0) = total money spent, defaults to 0 if no orders
    // 4. MAX(o.order_date) = the date of their most recent purchase
    // 5. Results are sorted by highest spender first (DESC)
    @Query(value = """
        SELECT c.customer_id,
               c.first_name || ' ' || c.last_name AS customer_name,
               c.city_name,
               COUNT(DISTINCT o.order_id)          AS total_orders,
               COALESCE(SUM(o.total_amount), 0)    AS total_spent,
               MAX(o.order_date)                   AS last_order_date
        FROM customers c
        LEFT JOIN orders o ON c.customer_id = o.customer_id
                          AND o.order_status != 'CANCELLED'
        GROUP BY c.customer_id, c.first_name, c.last_name, c.city_name
        ORDER BY total_spent DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getCustomerPurchaseSummary();
}
