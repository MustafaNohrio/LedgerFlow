package com.ledgerflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Maps to the "categories" table.
// Categories are used to group products together (e.g., "Electronics", "Clothing").
// Every product must belong to exactly one category.
@Entity
@Table(name = "categories")
@Data @NoArgsConstructor @AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_gen")
    @SequenceGenerator(name = "cat_gen", sequenceName = "seq_category_id", allocationSize = 1)
    @Column(name = "category_id")
    private Long categoryId;

    // @NotBlank = this field can never be empty or null
    // Category names must be unique so we don't end up with duplicates
    @NotBlank
    @Size(max = 100)
    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    // Optional description for the category
    @Column(name = "description", length = 500)
    private String description;
}
