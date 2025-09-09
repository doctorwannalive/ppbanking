package com.example.ppbanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegisterRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "username may contain letters, digits, dot, underscore, dash")
        String username,

        @NotBlank
        @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "password must contain at least 1 letter and 1 digit")
        String password,

        // Optional; defaults to ADMIN on the server if null/blank
        @Pattern(regexp = "(?i)^(USER|ADMIN)$", message = "role must be USER or ADMIN")
        String role
) {}
