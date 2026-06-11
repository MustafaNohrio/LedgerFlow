package com.ledgerflow.service;

import com.ledgerflow.model.*;
import com.ledgerflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// The core business logic for orders — placing, searching, updating status, and dashboard stats.
// This is probably the most important service in the entire project.
@Service
public class OrderService {

    @Autowired private OrderRepository    orderRepo;
    @Autowired private OrderItemRepository itemRepo;
    @Autowired private CustomerRepository  customerRepo;
    @Autowired private ProductRepository   productRepo;

    // Defines which status transitions are allowed.
    // For example, a PENDING order can go to CONFIRMED or CANCELLED,
    // but a DELIVERED order can't go anywhere — it's final.
    private static final Map<String, List<String>> ALLOWED_TRANSITIONS = Map.of(
        "PENDING",   List.of("CONFIRMED", "CANCELLED"),
        "CONFIRMED", List.of("SHIPPED",   "CANCELLED"),
        "SHIPPED",   List.of("DELIVERED"),
        "DELIVERED", List.of(),
        "CANCELLED", List.of()
    );

    // Search orders with optional filters — used by the orders list page
    public Page<Order> search(String status, Long customerId,
                               LocalDate from, LocalDate to,
                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepo.search(
            (status == null || status.isBlank()) ? null : status,
            customerId,
            from, to,
            pageable
        );
    }

    public Order getById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order #" + id + " not found"));
    }

    public List<Order> getByCustomer(Long customerId) {
        return orderRepo.findByCustomerCustomerIdOrderByOrderDateDesc(customerId);
    }

    // Places a new order — this is a @Transactional operation, meaning if anything
    // fails halfway through (like insufficient stock), ALL changes get rolled back.
    @Transactional
    public Order placeOrder(OrderRequest req, User user) {
        // Find the customer who is making the purchase
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Create a blank order first
        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedByUser(user);
        order.setRemarks(req.getRemarks());
        order.setOrderStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        Order saved = orderRepo.save(order);

        // Now add each product as an order item
        for (OrderRequest.ItemLine line : req.getItems()) {
            Product product = productRepo.findById(line.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + line.getProductId()));

            // Can't sell a deactivated product
            if (!"Y".equals(product.getIsActive()))
                throw new RuntimeException("'" + product.getProductName() + "' is not available for sale");

            // Check if we have enough stock
            if (product.getStockQuantity() < line.getQuantity())
                throw new RuntimeException("Not enough stock for '" + product.getProductName()
                        + "'. Available: " + product.getStockQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(saved);
            item.setProduct(product);
            item.setQuantity(line.getQuantity());
            item.setUnitPrice(product.getUnitPrice());
            itemRepo.save(item);
        }

        // Re-fetch the order to include all the items we just added
        return orderRepo.findById(saved.getOrderId()).orElse(saved);
    }

    // Move an order to the next status (e.g., PENDING -> CONFIRMED -> SHIPPED -> DELIVERED)
    @Transactional
    public Order updateStatus(Long orderId, String newStatus) {
        Order order = getById(orderId);
        String current = order.getOrderStatus();

        // Enforce the allowed transitions — can't skip steps or go backwards
        List<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, List.of());
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException(
                "Cannot move order from " + current + " to " + newStatus +
                ". Allowed next steps: " + (allowed.isEmpty() ? "none" : String.join(", ", allowed))
            );
        }

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepo.save(order);
    }

    // Collects all the numbers needed for the dashboard page in one go
    // Returns a Map that gets sent as JSON to the frontend
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders",     orderRepo.count());
        stats.put("pendingOrders",   orderRepo.countPending());
        stats.put("totalCustomers",  customerRepo.count());
        stats.put("revenueThisMonth", orderRepo.getRevenueThisMonth());
        stats.put("totalRevenue",    orderRepo.getTotalRevenue());
        stats.put("ordersByStatus",  orderRepo.countByStatus());
        stats.put("monthlySales",    orderRepo.getMonthlySales());
        stats.put("lowStockItems",   productRepo.findLowStock(10).size());
        stats.put("totalProducts",   productRepo.count());
        return stats;
    }
}
