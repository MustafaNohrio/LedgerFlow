package com.ledgerflow.service;

import com.ledgerflow.model.User;
import com.ledgerflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

// Simple service for user management — only Admins use this.
// The main feature here is toggling user accounts on/off.
@Service
public class UserService {

    @Autowired private UserRepository userRepo;

    // Get all staff accounts (shown in the Users management page)
    public List<User> getAll() { return userRepo.findAll(); }

    public User getById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Toggle a user's active status — if they're active ("Y"), deactivate them ("N"),
    // and vice versa. Deactivated users can't log in anymore.
    public User toggleActive(Long id) {
        User user = getById(id);
        user.setIsActive("Y".equals(user.getIsActive()) ? "N" : "Y");
        return userRepo.save(user);
    }
}
