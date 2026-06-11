package com.ledgerflow.repository;

import com.ledgerflow.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Data access for the "order_items" junction table.
// Used to fetch individual line items for a specific order.
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Spring auto-generates: SELECT * FROM order_items WHERE order_id = ?
    // The method name "findByOrderOrderId" means: navigate to the 'order' field,
    // then match on its 'orderId' property — Spring is smart enough to figure that out
    List<OrderItem> findByOrderOrderId(Long orderId);
}
