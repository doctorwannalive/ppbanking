package com.example.ppbanking.web;

import com.example.ppbanking.dto.*;
import com.example.ppbanking.security.AppUserDetailsService;
import com.example.ppbanking.security.JwtService;
import com.example.ppbanking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService account;
    private final JwtService jwt;
    private final AppUserDetailsService uds;
    private final PasswordEncoder encoder;

    // Публичная регистрация — всегда USER
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest req) {
        account.register(req.username(), req.password(), "USER");
        return ResponseEntity.ok().build();
    }

    // Админская регистрация:
    //  - если пользователей ещё нет → bootstrap: можно без токена создать первого ADMIN/USER
    //  - иначе — нужен токен c ролью ADMIN
    @PostMapping("/register-admin")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid AdminRegisterRequest req) {
        boolean hasUsers = account.hasAnyUsers();
        String desiredRole = (req.role() == null || req.role().isBlank()) ? "ADMIN" : req.role();

        if (!hasUsers) {
            account.register(req.username(), req.password(), desiredRole);
            return ResponseEntity.ok().build();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        account.register(req.username(), req.password(), desiredRole);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        var ud = uds.loadUserByUsername(req.username());
        if (!encoder.matches(req.password(), ud.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String access = jwt.generateAccess(req.username());
        String refresh = jwt.generateRefresh(req.username());
        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String,String> body) {
        String refresh = body.get("refreshToken");
        if (refresh == null) return ResponseEntity.badRequest().build();
        String username = jwt.extractUsername(refresh);
        String newAccess = jwt.generateAccess(username);
        return ResponseEntity.ok(new AuthResponse(newAccess, refresh));
    }
}
