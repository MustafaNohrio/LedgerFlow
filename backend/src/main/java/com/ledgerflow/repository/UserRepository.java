package com.ledgerflow.repository;

import com.ledgerflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Data access layer for the "users" table.
// Used by AuthService to look up users during login and registration.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login — find the user by their username
    Optional<User> findByUsername(String username);

    // Used during registration — check if email is already taken
    Optional<User> findByEmail(String email);
}
