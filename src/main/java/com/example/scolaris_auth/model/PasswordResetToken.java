package com.example.scolaris_auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;          // UUID généré

    private String username;      // email du user

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean used = false;
}