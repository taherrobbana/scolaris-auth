package com.example.scolaris_auth.dto.request;

import com.example.scolaris_auth.model.enums.Role;
import lombok.Data;

@Data
public class AdminUpdateRequest {
    private String username;
    private String firstName;
    private String lastName;
    private Role role;
    private String group;
    private String avatar;
    private Boolean deleted;
    private String newPassword;
}
