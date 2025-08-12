package com.example.ppbanking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(@NotNull Long toUserId, @Positive Double amount) {}
