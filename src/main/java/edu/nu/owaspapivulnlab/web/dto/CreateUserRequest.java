package edu.nu.owaspapivulnlab.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * FIX-6: DTO for creating users - prevents mass assignment
 * Does NOT include role or isAdmin fields
 */
public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        String email
) {}
