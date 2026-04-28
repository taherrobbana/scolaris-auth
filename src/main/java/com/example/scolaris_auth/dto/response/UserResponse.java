package com.example.scolaris_auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String keycloakId;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String group;
    private String avatar;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}