package com.example.ppbanking.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminRegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        String role // опционально: "ADMIN" или "USER"; если пусто — возьмём "ADMIN"
) {}
