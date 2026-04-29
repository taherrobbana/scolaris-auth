package com.example.scolaris_auth.service;

import com.example.scolaris_auth.dto.request.LoginRequest;
import com.example.scolaris_auth.dto.request.RegisterRequest;
import com.example.scolaris_auth.dto.response.AuthResponse;
import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.User;
import com.example.scolaris_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;

    // ─── Register ─────────────────────────────────────────────────────
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new AppException("Username already exists", 409);

        String keycloakId = keycloakService.createKeycloakUser(
                req.getUsername(), req.getPassword(),
                req.getFirstName(), req.getLastName(),
                req.getRole(), req.getGroup()
        );

        User user = User.builder()
                .keycloakId(keycloakId)
                .username(req.getUsername())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(req.getRole())
                .group(req.getGroup())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        AccessTokenResponse tokenResponse = keycloakService.getToken(
                req.getUsername(), req.getPassword());

        return AuthResponse.builder()
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .role(req.getRole() != null ? req.getRole().name() : null)
                .message("Account created successfully")
                .build();
    }

    // ─── Login ────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AppException("Invalid credentials", 401));

        if (user.isDeleted())
            throw new AppException("Account is deactivated", 403);

        AccessTokenResponse tokenResponse = keycloakService.getToken(
                req.getUsername(), req.getPassword());

        return AuthResponse.builder()
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    // ─── Refresh token ────────────────────────────────────────────────
    public AuthResponse refreshToken(String refreshToken) {
        AccessTokenResponse tokenResponse =
                keycloakService.refreshToken(refreshToken);
        return AuthResponse.builder()
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .message("Token refreshed")
                .build();
    }
}