package com.example.ppbanking.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
        @NotNull @Positive Long toUserId,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) Double amount
) {}
