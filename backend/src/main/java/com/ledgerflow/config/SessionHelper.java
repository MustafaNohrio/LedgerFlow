package com.ledgerflow.config;

import com.ledgerflow.model.Role;
import com.ledgerflow.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

// Utility class for session-based authentication and role-based access control.
// Every controller calls these methods before processing a request to check
// whether the user is logged in and whether they have the required role.
//
// Currently returns a mock admin user for development — in production,
// you'd uncomment the real session lookup (see getUser method).
public class SessionHelper {

    // The key we use to store the user object in the HTTP session
    public static final String SESSION_KEY = "current_user";

    private SessionHelper() {}

    // Returns the currently logged-in user from the session.
    // For local development, we return a mock admin user so we don't
    // have to log in every time we restart the server.
    public static User getUser(HttpSession session) {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("admin");
        mockUser.setFullName("Test Admin");
        mockUser.setEmail("admin@test.com");
        
        Role mockRole = new Role();
        mockRole.setRoleName("ADMIN");
        mockUser.setRole(mockRole);
        
        return mockUser;
        // In production, replace the above with:
        // return (User) session.getAttribute(SESSION_KEY);
    }

    // Standard 401 response for unauthenticated requests
    public static ResponseEntity<?> notLoggedIn() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Please log in to continue"));
    }

    // Standard 403 response for unauthorized requests (wrong role)
    public static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "You don't have permission to do that"));
    }

    // Check if the user has one of the specified roles
    public static boolean hasRole(User user, String... roles) {
        if (user == null || user.getRole() == null) return false;
        String current = user.getRole().getRoleName();
        for (String r : roles) {
            if (r.equalsIgnoreCase(current)) return true;
        }
        return false;
    }

    // Gate: returns an error response if user isn't logged in, null if they are
    // Controllers use this like: if (check != null) return check;
    public static ResponseEntity<?> requireLogin(HttpSession session) {
        if (getUser(session) == null) return notLoggedIn();
        return null;
    }

    // Gate: returns an error response if user doesn't have the right role
    public static ResponseEntity<?> requireRole(HttpSession session, String... roles) {
        User user = getUser(session);
        if (user == null) return notLoggedIn();
        if (!hasRole(user, roles)) return forbidden();
        return null;
    }
}
