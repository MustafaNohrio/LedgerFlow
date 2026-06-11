package com.ledgerflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// This class maps to the "users" table.
// Users are the staff members (admin, manager, cashier, sales agent) who log in to the system.
// Note: Customers are stored separately — they don't have login accounts.
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {

    // Primary Key — auto-generated from the seq_user_id sequence
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "seq_user_id", allocationSize = 1)
    @Column(name = "user_id")
    private Long userId;

    // Foreign Key relationship — each user has exactly one role
    // FetchType.EAGER means: whenever we load a User, always load their Role too
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    // Username must be unique across the entire system
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    // @JsonIgnore prevents the password hash from ever being sent to the frontend
    // This is a security measure — we never want to expose passwords in API responses
    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    // "Y" = active account, "N" = deactivated (can't log in anymore)
    // columnDefinition matches the Oracle schema: CHAR(1)
    @Column(name = "is_active", columnDefinition = "CHAR(1)")
    private String isActive = "Y";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
