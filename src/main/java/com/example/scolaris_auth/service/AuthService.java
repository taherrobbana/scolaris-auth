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
                .group(req.getGroup())
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .birthPlace(user.getBirthPlace())
                .nationality(user.getNationality())
                .phone(user.getPhone())
                .address(user.getAddress())
                .postalCode(user.getPostalCode())
                .city(user.getCity())
                .country(user.getCountry())
                .emergencyContacts(user.getEmergencyContacts())
                .message("Account created successfully")
                .build();
    }

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
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .group(user.getGroup())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .birthPlace(user.getBirthPlace())
                .nationality(user.getNationality())
                .phone(user.getPhone())
                .address(user.getAddress())
                .postalCode(user.getPostalCode())
                .city(user.getCity())
                .country(user.getCountry())
                .emergencyContacts(user.getEmergencyContacts())
                .message("Login successful")
                .build();
    }

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