package edu.nu.owaspapivulnlab.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * FIX-9: DTO for transfer request with validation
 */
public record TransferRequest(
        @NotNull
        @Positive(message = "Transfer amount must be positive")
        Double amount
) {}
