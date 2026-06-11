package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.model.*;
import com.ledgerflow.repository.RoleRepository;
import com.ledgerflow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// API endpoints for authentication: login, logout, register, and "who am I?"
// All routes start with /api/auth/
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService  authService;
    @Autowired private RoleRepository roleRepo;

    // POST /api/auth/login — called when someone submits the login form
    // On success: returns user info (id, name, role) as JSON
    // On failure: returns 401 Unauthorized with error message
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        try {
            User user = authService.login(req.getUsername(), req.getPassword(), session);
            return ResponseEntity.ok(Map.of(
                "userId",   user.getUserId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role",     user.getRole().getRoleName(),
                "email",    user.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/logout — clears the session
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // GET /api/auth/me — returns the currently logged-in user's info
    // The frontend calls this on page load to check if the session is still valid
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        User user = SessionHelper.getUser(session);
        if (user == null) return SessionHelper.notLoggedIn();
        return ResponseEntity.ok(Map.of(
            "userId",   user.getUserId(),
            "username", user.getUsername(),
            "fullName", user.getFullName(),
            "role",     user.getRole().getRoleName(),
            "email",    user.getEmail()
        ));
    }

    // POST /api/auth/register — creates a new staff account
    // Only ADMIN users can access this endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN");
        if (check != null) return check;
        try {
            User created = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "userId",   created.getUserId(),
                "username", created.getUsername(),
                "message",  "Account created successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/auth/roles — returns the list of available roles
    // Used to populate the role dropdown in the "Create User" form
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN");
        if (check != null) return check;
        return ResponseEntity.ok(roleRepo.findAll());
    }
}
