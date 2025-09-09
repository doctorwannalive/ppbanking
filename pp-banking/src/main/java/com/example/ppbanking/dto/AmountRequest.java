package com.example.ppbanking.dto;

import jakarta.validation.constraints.Positive;

public record AmountRequest(@Positive Double amount) {}
