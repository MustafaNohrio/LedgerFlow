package com.ledgerflow.service;

import com.ledgerflow.model.*;
import com.ledgerflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Business logic for managing products — CRUD operations plus stock management.
@Service
public class ProductService {

    @Autowired private ProductRepository  productRepo;
    @Autowired private CategoryRepository categoryRepo;

    // Search with optional filters — delegates to the repository query
    public Page<Product> search(String q, Long categoryId, String active, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepo.search(
            (q == null || q.isBlank()) ? null : q,
            categoryId,
            (active == null || active.isBlank()) ? null : active,
            pageable
        );
    }

    public Product getById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // Get products with stock <= 10 (used in dashboard warning)
    public List<Product> getLowStock() {
        return productRepo.findLowStock(10);
    }

    // Get best-selling products based on order history
    public List<Product> getTopSelling() {
        return productRepo.findTopSelling();
    }

    // Add a new product to the inventory
    public Product create(Product p, Long userId) {
        // Make sure the category actually exists
        categoryRepo.findById(p.getCategory().getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check for duplicate SKU codes — each product must have a unique SKU
        productRepo.findBySkuCode(p.getSkuCode()).ifPresent(existing -> {
            throw new RuntimeException("SKU code '" + p.getSkuCode() + "' is already in use");
        });

        // Track who created this product (for the audit_log trigger in the DB)
        p.setUpdatedByUserId(userId);
        return productRepo.save(p);
    }

    // Update an existing product's details
    public Product update(Long id, Product updated, Long userId) {
        Product existing = getById(id);
        existing.setProductName(updated.getProductName());
        existing.setUnitPrice(updated.getUnitPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setIsActive(updated.getIsActive());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedByUserId(userId);

        // Update category if a new one was provided
        if (updated.getCategory() != null && updated.getCategory().getCategoryId() != null) {
            categoryRepo.findById(updated.getCategory().getCategoryId())
                    .ifPresent(existing::setCategory);
        }
        return productRepo.save(existing);
    }

    // Soft-delete: we don't actually remove the product from the database,
    // we just set is_active to "N" so it stops appearing in the store
    public void deactivate(Long id, Long userId) {
        Product p = getById(id);
        p.setIsActive("N");
        p.setUpdatedByUserId(userId);
        p.setUpdatedAt(LocalDateTime.now());
        productRepo.save(p);
    }
}
