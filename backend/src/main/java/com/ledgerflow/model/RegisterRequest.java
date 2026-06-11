package com.ledgerflow.model;

import jakarta.validation.constraints.*;
import lombok.Data;

// DTO for creating a new staff account.
// Only Admins can register new users — the roleId decides what access they get.
// Password must be at least 6 characters long.
@Data
public class RegisterRequest {
    @NotBlank private String fullName;
    @NotBlank private String username;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank @Email private String email;
    @NotNull  private Long roleId;
}
