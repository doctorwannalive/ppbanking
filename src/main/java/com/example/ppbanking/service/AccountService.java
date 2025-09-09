package com.example.ppbanking.service;

import com.example.ppbanking.domain.Transaction;
import com.example.ppbanking.domain.User;
import com.example.ppbanking.dto.AccountResponse;
import com.example.ppbanking.exception.ApiException;
import com.example.ppbanking.repo.TransactionRepository;
import com.example.ppbanking.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    /** Create a user with an explicit role (USER or ADMIN). */
    public void register(String username, String rawPassword, String role) {
        users.findByUsername(username).ifPresent(u -> {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "Username is already taken");
        });

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setBalance(0.0);
        u.setRole(normalizeRole(role)); // only USER or ADMIN are allowed
        users.save(u);
    }

    /** Normalize role to USER or ADMIN; throw 400 otherwise. */
    private String normalizeRole(String role) {
        if (role == null) return "USER";
        String r = role.trim().toUpperCase();
        if (!r.equals("USER") && !r.equals("ADMIN")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_INVALID", "Role must be USER or ADMIN");
        }
        return r;
    }

    /** True if at least one user exists (used for admin bootstrap logic). */
    public boolean hasAnyUsers() {
        return users.count() > 0;
    }

    /** Find user by username or 404. */
    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    /** Get account balance and history for a user or 404. */
    public AccountResponse getAccount(Long userId) {
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
        return new AccountResponse(u.getBalance(), txs.findAllForUser(userId));
    }

    /** Deposit a positive amount into the user's balance. */
    public void deposit(Long userId, double amount) {
        if (amount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AMOUNT_INVALID", "Amount must be > 0");
        }
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        u.setBalance(u.getBalance() + amount);
        users.save(u);
        recordTx(userId, userId, amount, "DEPOSIT");
    }

    /** Withdraw a positive amount if sufficient funds exist. */
    public void withdraw(Long userId, double amount) {
        if (amount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AMOUNT_INVALID", "Amount must be > 0");
        }
        User u = users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        if (u.getBalance() < amount) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", "Insufficient funds");
        }

        u.setBalance(u.getBalance() - amount);
        users.save(u);
        recordTx(userId, userId, amount, "WITHDRAW");
    }

    /** Transfer a positive amount from one user to another. */
    public void transfer(Long fromId, Long toId, double amount) {
        if (Objects.equals(fromId, toId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SELF_TRANSFER", "Cannot transfer to the same account");
        }
        if (amount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AMOUNT_INVALID", "Amount must be > 0");
        }

        User from = users.findById(fromId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SENDER_NOT_FOUND", "Sender not found"));
        User to = users.findById(toId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RECEIVER_NOT_FOUND", "Receiver not found"));

        if (from.getBalance() < amount) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", "Insufficient funds");
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        users.save(from);
        users.save(to);
        recordTx(fromId, toId, amount, "TRANSFER");
    }

    /** Persist a transaction record. */
    private void recordTx(Long senderId, Long receiverId, double amount, String type) {
        Transaction t = new Transaction();
        t.setSenderId(senderId);
        t.setReceiverId(receiverId);
        t.setAmount(amount);
        t.setType(type);
        txs.save(t);
    }
}
