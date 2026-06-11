package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.model.Customer;
import com.ledgerflow.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// API endpoints for customer management — search, create, update, and CSV export.
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired private CustomerService customerService;

    // GET /api/customers?q=john&city=karachi&page=0&size=20
    // Returns a paginated list of customers, with optional name and city filters
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(customerService.search(q, city, page, size));
    }

    // GET /api/customers/5 — get a single customer's full details
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        try {
            return ResponseEntity.ok(customerService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/customers — add a new customer to the system
    // ADMIN, MANAGER, and SALES_AGENT can add customers
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Customer customer, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER", "SALES_AGENT");
        if (check != null) return check;
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(customer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/customers/5 — update an existing customer's info
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Customer customer,
                                    HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER", "SALES_AGENT");
        if (check != null) return check;
        try {
            return ResponseEntity.ok(customerService.update(id, customer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/customers/export/csv — download all customers as a CSV spreadsheet
    @GetMapping("/export/csv")
    public void exportCsv(HttpSession session, HttpServletResponse response) throws IOException {
        if (SessionHelper.requireLogin(session) != null) {
            response.setStatus(401);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("Customer ID,First Name,Last Name,Email,Phone,City");
        customerService.search(null, null, 0, 10000).getContent().forEach(c ->
            writer.printf("%d,%s,%s,%s,%s,%s%n",
                c.getCustomerId(), c.getFirstName(), c.getLastName(),
                c.getEmailAddress() != null ? c.getEmailAddress() : "",
                c.getPhoneNumber()  != null ? c.getPhoneNumber()  : "",
                c.getCityName()     != null ? c.getCityName()     : "")
        );
    }
}
