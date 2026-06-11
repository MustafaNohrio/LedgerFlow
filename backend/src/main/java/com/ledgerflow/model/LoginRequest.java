package com.ledgerflow.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// This is a DTO (Data Transfer Object) — it's not a database table.
// It simply holds the username and password that the frontend sends
// when someone clicks the "Login" button.
@Data
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
