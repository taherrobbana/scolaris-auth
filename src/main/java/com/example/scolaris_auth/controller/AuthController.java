package com.example.scolaris_auth.controller;

import com.example.scolaris_auth.dto.request.ForgotPasswordRequest;
import com.example.scolaris_auth.dto.request.LoginRequest;
import com.example.scolaris_auth.dto.request.RegisterRequest;
import com.example.scolaris_auth.dto.request.ResetPasswordRequest;
import com.example.scolaris_auth.dto.response.AuthResponse;
import com.example.scolaris_auth.service.AuthService;
import com.example.scolaris_auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                authService.refreshToken(body.get("refreshToken")));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.sendResetEmail(req.getEmail());
        return ResponseEntity.ok(
                Map.of("message", "If this email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.getCode(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}