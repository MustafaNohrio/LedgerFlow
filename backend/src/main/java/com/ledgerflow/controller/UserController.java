package com.ledgerflow.controller;

import com.ledgerflow.config.SessionHelper;
import com.ledgerflow.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// API endpoints for user (staff) management.
// Only ADMIN can access these — they see all accounts and can enable/disable them.
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    // GET /api/users — list all staff accounts (shown in the Users page)
    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN");
        if (check != null) return check;
        return ResponseEntity.ok(userService.getAll());
    }

    // PATCH /api/users/3/toggle — activate or deactivate a user account
    // If they're active, this deactivates them (they can't log in anymore)
    // If they're inactive, this reactivates them
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleActive(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> check = SessionHelper.requireRole(session, "ADMIN");
        if (check != null) return check;
        try {
            return ResponseEntity.ok(userService.toggleActive(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
