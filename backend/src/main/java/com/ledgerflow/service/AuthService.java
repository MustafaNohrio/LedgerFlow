package com.ledgerflow.service;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.model.*;
import com.ledgerflow.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// Handles all authentication logic: login, logout, and new user registration.
// Passwords are never stored as plain text — we hash them with BCrypt first.
@Service
public class AuthService {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;

    // BCrypt is a one-way hashing algorithm — you can verify a password against
    // a hash, but you can never reverse the hash back to the original password
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    // Validates credentials and creates a session for the user
    public User login(String username, String password, HttpSession session) {
        // Step 1: find the user by username, throw error if not found
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Step 2: check if the account is active
        if (!"Y".equals(user.getIsActive()))
            throw new RuntimeException("This account has been deactivated");

        // Step 3: compare the entered password with the stored hash
        // bcrypt.matches() does the comparison — we never decrypt the hash
        if (!bcrypt.matches(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid username or password");

        // Step 4: store the user in the HTTP session so we remember they're logged in
        session.setAttribute(SessionHelper.SESSION_KEY, user);
        return user;
    }

    // Destroys the session — user will need to log in again
    public void logout(HttpSession session) {
        session.invalidate();
    }

    // Creates a new staff account (only Admins can do this)
    public User register(RegisterRequest req) {
        // Make sure username isn't already taken
        if (userRepo.findByUsername(req.getUsername()).isPresent())
            throw new RuntimeException("Username '" + req.getUsername() + "' is already taken");

        // Make sure email isn't already used
        if (userRepo.findByEmail(req.getEmail()).isPresent())
            throw new RuntimeException("An account with that email already exists");

        // Look up the role they selected (Admin, Manager, Cashier, etc.)
        Role role = roleRepo.findById(req.getRoleId())
                .orElseThrow(() -> new RuntimeException("Selected role does not exist"));

        // Build the new user — hash the password before saving
        User user = new User();
        user.setRole(role);
        user.setFullName(req.getFullName());
        user.setUsername(req.getUsername());
        user.setPasswordHash(bcrypt.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setIsActive("Y");

        return userRepo.save(user);
    }
}
