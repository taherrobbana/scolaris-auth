package com.example.scolaris_auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank @Size(min = 8)
    private String newPassword;
}