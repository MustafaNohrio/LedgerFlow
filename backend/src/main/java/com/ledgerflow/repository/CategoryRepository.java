package com.ledgerflow.repository;

import com.ledgerflow.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Data access layer for the "categories" table.
// Pretty simple — just basic CRUD plus a lookup by name.
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Check if a category with this name already exists (used to prevent duplicates)
    Optional<Category> findByCategoryName(String categoryName);
}
