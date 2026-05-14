package com.example.scolaris_auth.dto.request;

import com.example.scolaris_auth.model.enums.Role;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String avatar;
    private String group;
    private Role role;
    private String gender;
    private String birthDate;
    private String birthPlace;
    private String nationality;

    private String phone;
    private String address;
    private String postalCode;
    private String city;
    private String country;

    private java.util.List<com.example.scolaris_auth.model.EmergencyContact> emergencyContacts;
}
