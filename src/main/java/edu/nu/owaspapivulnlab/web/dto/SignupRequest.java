package edu.nu.owaspapivulnlab.web.dto;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
