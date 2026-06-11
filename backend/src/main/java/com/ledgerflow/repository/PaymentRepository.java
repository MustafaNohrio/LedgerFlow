package com.ledgerflow.repository;

import com.ledgerflow.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

// Data access for the "payments" table.
// Handles recording payments and calculating how much has been paid so far on an order.
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Get all payment records for a specific order
    List<Payment> findByOrderOrderId(Long orderId);

    // Calculate the total amount already paid on a specific order.
    // COALESCE returns 0 if there are no payments yet (instead of NULL).
    // We only count COMPLETED payments — failed or pending ones don't count.
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.order.orderId = :orderId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal getTotalPaidForOrder(@Param("orderId") Long orderId);
}
