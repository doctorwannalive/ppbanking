package com.example.ppbanking.service;

import com.example.ppbanking.domain.Transaction;
import com.example.ppbanking.domain.User;
import com.example.ppbanking.dto.AccountResponse;
import com.example.ppbanking.repo.TransactionRepository;
import com.example.ppbanking.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final UserRepository users;
    private final TransactionRepository txs;
    private final PasswordEncoder encoder;

    // ===== Регистрация с явной ролью =====
    public void register(String username, String rawPassword, String role) {
        users.findByUsername(username).ifPresent(u -> { throw new IllegalArgumentException("Username taken"); });
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setBalance(0.0);
        u.setRole(normalizeRole(role)); // "USER" | "ADMIN"
        users.save(u);
    }

    private String normalizeRole(String role) {
        if (role == null) return "USER";
        String r = role.trim().toUpperCase();
        if (!r.equals("USER") && !r.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role");
        }
        return r;
    }

    public boolean hasAnyUsers() {
        return users.count() > 0;
    }

    // ===== Утилиты пользователя/аккаунта =====
    public User getByUsername(String username) {
        return users.findByUsername(username).orElseThrow();
    }

    public AccountResponse getAccount(Long userId) {
        User u = users.findById(userId).orElseThrow();
        return new AccountResponse(u.getBalance(), txs.findAllForUser(userId));
    }

    // ===== Операции с балансом =====
    public void deposit(Long userId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        User u = users.findById(userId).orElseThrow();
        u.setBalance(u.getBalance() + amount);
        users.save(u);
        recordTx(userId, userId, amount, "DEPOSIT");
    }

    public void withdraw(Long userId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        User u = users.findById(userId).orElseThrow();
        if (u.getBalance() < amount) throw new IllegalStateException("Insufficient funds");
        u.setBalance(u.getBalance() - amount);
        users.save(u);
        recordTx(userId, userId, amount, "WITHDRAW");
    }

    public void transfer(Long fromId, Long toId, double amount) {
        if (Objects.equals(fromId, toId)) throw new IllegalArgumentException("Same account");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        User from = users.findById(fromId).orElseThrow();
        User to = users.findById(toId).orElseThrow();
        if (from.getBalance() < amount) throw new IllegalStateException("Insufficient funds");
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        users.save(from);
        users.save(to);
        recordTx(fromId, toId, amount, "TRANSFER");
    }

    private void recordTx(Long s, Long r, double amt, String type) {
        Transaction t = new Transaction();
        t.setSenderId(s);
        t.setReceiverId(r);
        t.setAmount(amt);
        t.setType(type);
        txs.save(t);
    }
}
