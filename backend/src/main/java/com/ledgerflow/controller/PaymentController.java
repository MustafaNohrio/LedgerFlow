package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

// API endpoints for recording and viewing payments.
// Payments are always linked to an order.
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired private PaymentService paymentService;

    // GET /api/payments — list all payment records
    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(paymentService.getAll());
    }

    // GET /api/payments/order/123 — all payments for a specific order
    // Used in the order detail view to show payment history
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> byOrder(@PathVariable Long orderId, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(paymentService.getByOrder(orderId));
    }

    // POST /api/payments — record a new payment against an order
    // Only ADMIN, MANAGER, and CASHIER can record payments
    @PostMapping
    public ResponseEntity<?> record(@RequestBody Map<String, Object> body, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER", "CASHIER");
        if (check != null) return check;
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            BigDecimal amount = new BigDecimal(body.get("amountPaid").toString());
            String method = body.get("paymentMethod").toString();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paymentService.record(orderId, amount, method));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
