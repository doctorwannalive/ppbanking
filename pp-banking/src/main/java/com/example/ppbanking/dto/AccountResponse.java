package com.example.ppbanking.dto;

import com.example.ppbanking.domain.Transaction;

import java.util.List;

public record AccountResponse(Double balance, List<Transaction> history) {}
