package com.banking.auth_service.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.auth_service.dto.AuthResponse;
import com.banking.auth_service.dto.LoginRequest;
import com.banking.auth_service.dto.RegisterRequest;
import com.banking.auth_service.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register — naya user banao
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    // POST /api/auth/login — login karo, token lo
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // GET /api/auth/validate?token=xxx — gateway yeh use karega
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestParam String token) {
        boolean valid = authService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    // GET /api/auth/health — basic check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Auth Service Running"));
    }
}
