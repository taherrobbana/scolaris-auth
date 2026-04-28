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
public class GroupResponse {
    private String id;
    private String name;
    private String keycloakGroupId;
    private LocalDateTime createdAt;
}
