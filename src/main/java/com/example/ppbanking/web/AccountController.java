package com.example.ppbanking.web;

import com.example.ppbanking.dto.*;
import com.example.ppbanking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return service.getByUsername(username).getId();
    }

    @GetMapping
    public AccountResponse me() {
        return service.getAccount(currentUserId());
    }

    @PostMapping("/deposit")
    public void deposit(@RequestBody @Valid AmountRequest req) {
        service.deposit(currentUserId(), req.amount());
    }

    @PostMapping("/withdraw")
    public void withdraw(@RequestBody @Valid AmountRequest req) {
        service.withdraw(currentUserId(), req.amount());
    }

    @PostMapping("/transfer")
    public void transfer(@RequestBody @Valid TransferRequest req) {
        service.transfer(currentUserId(), req.toUserId(), req.amount());
    }
}
