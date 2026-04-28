package com.example.scolaris_auth.dto.request;

import lombok.Data;

@Data
public class UpdateSelfRequest {
    // username, role, groupe interdits — non présents ici intentionnellement
    private String firstName;
    private String lastName;
    private String avatar;  // base64
}