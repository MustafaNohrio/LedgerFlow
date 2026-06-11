package com.ledgerflow.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// This class maps to the "roles" table in our database.
// Each role represents an access level like ADMIN, MANAGER, CASHIER, or SALES_AGENT.
// We use roles to control what each user is allowed to do in the system.
@Entity
@Table(name = "roles")
@Data @NoArgsConstructor @AllArgsConstructor
public class Role {

    // Primary Key — auto-generated using a database sequence
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_gen")
    @SequenceGenerator(name = "role_gen", sequenceName = "seq_role_id", allocationSize = 1)
    @Column(name = "role_id")
    private Long roleId;

    // The name of the role (e.g., "ADMIN", "CASHIER")
    // Must be unique — we can't have two roles with the same name
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;
}
