package com.ledgerflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// Maps to the "customers" table.
// Customers are the people who buy products from our store.
// They do NOT have login accounts — only staff members (users) can log in.
@Entity
@Table(name = "customers")
@Data @NoArgsConstructor @AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cust_gen")
    @SequenceGenerator(name = "cust_gen", sequenceName = "seq_customer_id", allocationSize = 1)
    @Column(name = "customer_id")
    private Long customerId;

    @NotBlank @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Email is optional but must be unique if provided
    @Email
    @Column(name = "email_address", unique = true, length = 255)
    private String emailAddress;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "city_name", length = 100)
    private String cityName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
