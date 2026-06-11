package com.ledgerflow.repository;

import com.ledgerflow.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Data access layer for the "roles" table.
// JpaRepository gives us free CRUD methods (findAll, findById, save, delete)
// without writing any SQL — Spring generates them automatically.
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Custom finder — Spring auto-generates the SQL behind the scenes:
    // SELECT * FROM roles WHERE role_name = ?
    Optional<Role> findByRoleName(String roleName);
}
