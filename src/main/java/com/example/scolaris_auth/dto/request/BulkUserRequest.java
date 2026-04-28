package com.example.scolaris_auth.dto.request;

import com.example.scolaris_auth.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class BulkUserRequest {
    @NotBlank @Email
    private String username;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank private String firstName;
    @NotBlank private String lastName;

    @NotNull private Role role;

    private String group;   // optionnel
    private String avatar;   // optionnel, base64
    private Boolean deleted; // optionnel
}