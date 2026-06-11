package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.model.*;
import com.ledgerflow.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Map;

// API endpoints for managing orders — the busiest controller in the project.
// Handles: listing, filtering, placing new orders, changing status, dashboard stats, and CSV export.
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderService orderService;

    // GET /api/orders?status=&customerId=&from=&to=&page=0&size=20
    // Fetches a paginated, filtered list of orders — all filters are optional
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(orderService.search(status, customerId, from, to, page, size));
    }

    // GET /api/orders/123 — get a single order with all its items
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        try {
            return ResponseEntity.ok(orderService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/orders/customer/5 — all orders for a specific customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> byCustomer(@PathVariable Long customerId, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(orderService.getByCustomer(customerId));
    }

    // GET /api/orders/dashboard — returns all the stats the dashboard needs
    // Only ADMIN and MANAGER can see the full dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(orderService.getDashboardStats());
    }

    // POST /api/orders — place a new order
    // Only ADMIN, MANAGER, and CASHIER can create orders
    @PostMapping
    public ResponseEntity<?> place(@Valid @RequestBody OrderRequest req, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER", "CASHIER");
        if (check != null) return check;
        try {
            User user = SessionHelper.getUser(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(req, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PATCH /api/orders/123/status — update an order's status (e.g., PENDING -> CONFIRMED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER", "CASHIER");
        if (check != null) return check;
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "status field is required"));
        try {
            return ResponseEntity.ok(orderService.updateStatus(id, newStatus));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/orders/export/csv — download all orders as a CSV file
    @GetMapping("/export/csv")
    public void exportCsv(HttpSession session, HttpServletResponse response) throws IOException {
        if (SessionHelper.requireRole(session, "ADMIN", "MANAGER") != null) {
            response.setStatus(403);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("Order ID,Customer,Date,Status,Total");
        Page<Order> orders = orderService.search(null, null, null, null, 0, 100000);
        for (Order o : orders.getContent()) {
            writer.printf("%d,\"%s %s\",%s,%s,%.2f%n",
                o.getOrderId(),
                o.getCustomer().getFirstName(), o.getCustomer().getLastName(),
                o.getOrderDate(), o.getOrderStatus(), o.getTotalAmount());
        }
    }
}
