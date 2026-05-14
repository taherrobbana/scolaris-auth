package com.example.scolaris_auth.model;

import com.example.scolaris_auth.model.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    private String keycloakId;
    @Indexed(unique = true)
    private String username;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String group;
    private String avatar;
    private String gender;
    private String birthDate;
    private String birthPlace;
    private String nationality;
    private String phone;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private java.util.List<EmergencyContact> emergencyContacts;
    @Builder.Default
    private boolean deleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}