package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.model.*;
import com.ledgerflow.repository.CategoryRepository;
import com.ledgerflow.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

// API endpoints for product management — CRUD, stock alerts, and CSV export.
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductService    productService;
    @Autowired private CategoryRepository categoryRepo;

    // GET /api/products?q=laptop&categoryId=1&active=Y&page=0&size=20
    // Main product listing with optional search and filter parameters
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(productService.search(q, categoryId, active, page, size));
    }

    // GET /api/products/categories — list all categories for the filter dropdown
    @GetMapping("/categories")
    public ResponseEntity<?> categories(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    // GET /api/products/low-stock — products with stock <= 10
    // Only ADMIN and MANAGER can see this (it's business-sensitive info)
    @GetMapping("/low-stock")
    public ResponseEntity<?> lowStock(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        return ResponseEntity.ok(productService.getLowStock());
    }

    // GET /api/products/top-selling — best selling products by quantity sold
    @GetMapping("/top-selling")
    public ResponseEntity<?> topSelling(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        return ResponseEntity.ok(productService.getTopSelling());
    }

    // GET /api/products/5 — single product details
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireLogin(session);
        if (check != null) return check;
        try {
            return ResponseEntity.ok(productService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/products — add a new product to inventory
    // Only ADMIN and MANAGER can add products
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Product product, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        try {
            User user = SessionHelper.getUser(session);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productService.create(product, user.getUserId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/products/5 — update product name, price, stock, etc.
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Product product,
                                    HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN", "MANAGER");
        if (check != null) return check;
        try {
            User user = SessionHelper.getUser(session);
            return ResponseEntity.ok(productService.update(id, product, user.getUserId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/products/5 — soft-delete (marks as inactive, doesn't actually remove)
    // Only ADMIN can deactivate products
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN");
        if (check != null) return check;
        try {
            User user = SessionHelper.getUser(session);
            productService.deactivate(id, user.getUserId());
            return ResponseEntity.ok(Map.of("message", "Product deactivated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/products/export/csv — download product inventory as CSV spreadsheet
    @GetMapping("/export/csv")
    public void exportCsv(HttpSession session, HttpServletResponse response) throws IOException {
        if (SessionHelper.requireRole(session, "ADMIN", "MANAGER") != null) {
            response.setStatus(403);
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"products.csv\"");

        Page<Product> all = productService.search(null, null, null, 0, 10000);
        PrintWriter writer = response.getWriter();
        writer.println("Product ID,Name,SKU,Category,Unit Price,Stock,Active");
        for (Product p : all.getContent()) {
            writer.printf("%d,\"%s\",%s,%s,%.2f,%d,%s%n",
                p.getProductId(), p.getProductName(), p.getSkuCode(),
                p.getCategory().getCategoryName(), p.getUnitPrice(),
                p.getStockQuantity(), p.getIsActive());
        }
    }
}
