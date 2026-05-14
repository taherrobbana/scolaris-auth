package com.example.scolaris_auth.service;

import com.example.scolaris_auth.dto.request.AdminUpdateRequest;
import com.example.scolaris_auth.dto.request.BulkUserRequest;
import com.example.scolaris_auth.dto.request.ProfileUpdateRequest;
import com.example.scolaris_auth.dto.response.PageResponse;
import com.example.scolaris_auth.dto.response.UserResponse;
import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.User;
import com.example.scolaris_auth.model.enums.Role;
import com.example.scolaris_auth.repository.GroupRepository;
import com.example.scolaris_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public Map<String, Object> bulkCreate(List<BulkUserRequest> requests) {
        List<String> created = new ArrayList<>();
        List<Map<String, String>> failed = new ArrayList<>();

        for (BulkUserRequest req : requests) {
            try {
                if (userRepository.existsByUsername(req.getUsername())) {
                    failed.add(Map.of(
                            "username", req.getUsername(),
                            "error", "Username already exists"));
                    continue;
                }

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
                        .avatar(req.getAvatar())
                        .deleted(Boolean.TRUE.equals(req.getDeleted()))
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(user);
                created.add(req.getUsername());

            } catch (Exception e) {
                failed.add(Map.of(
                        "username", req.getUsername(),
                        "error", e.getMessage()));
            }
        }

        return Map.of(
                "created", created,
                "failed", failed,
                "totalCreated", created.size(),
                "totalFailed", failed.size()
        );
    }

    public List<UserResponse> adminUpdate(Map<String, AdminUpdateRequest> updates) {
        List<UserResponse> results = new ArrayList<>();

        for (Map.Entry<String, AdminUpdateRequest> entry : updates.entrySet()) {
            String keycloakId = entry.getKey();
            AdminUpdateRequest req = entry.getValue();

            User user = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() ->
                            new AppException("User not found: " + keycloakId, 404));

            keycloakService.updateKeycloakUser(
                    keycloakId,
                    req.getFirstName(),
                    req.getLastName(),
                    req.getUsername(),
                    req.getGroup()
            );

            if (req.getRole() != null)
                keycloakService.changeUserRole(keycloakId, req.getRole());

            if (req.getNewPassword() != null)
                keycloakService.resetKeycloakPassword(keycloakId, req.getNewPassword());

            if (req.getUsername()  != null) user.setUsername(req.getUsername());
            if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
            if (req.getLastName()  != null) user.setLastName(req.getLastName());
            if (req.getRole()      != null) user.setRole(req.getRole());
            if (req.getGroup()     != null) user.setGroup(req.getGroup());
            if (req.getAvatar()    != null) user.setAvatar(req.getAvatar());
            if (req.getDeleted()   != null) user.setDeleted(req.getDeleted());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            results.add(toUserResponse(user));
        }

        return results;
    }

    public UserResponse updateUser(String id, AdminUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found: " + id, 404));

        String keycloakId = user.getKeycloakId();

        keycloakService.updateKeycloakUser(
                keycloakId,
                req.getFirstName(),
                req.getLastName(),
                req.getUsername(),
                req.getGroup()
        );

        if (req.getRole() != null)
            keycloakService.changeUserRole(keycloakId, req.getRole());

        if (req.getNewPassword() != null)
            keycloakService.resetKeycloakPassword(keycloakId, req.getNewPassword());

        if (req.getUsername()  != null) user.setUsername(req.getUsername());
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) user.setLastName(req.getLastName());
        if (req.getRole()      != null) user.setRole(req.getRole());
        if (req.getGroup()     != null) user.setGroup(req.getGroup());
        if (req.getAvatar()    != null) user.setAvatar(req.getAvatar());
        if (req.getDeleted()   != null) user.setDeleted(req.getDeleted());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toUserResponse(user);
    }

    public UserResponse updateProfile(ProfileUpdateRequest req, Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException("User not found", 404));

        keycloakService.updateKeycloakUser(
                keycloakId,
                req.getFirstName(),
                req.getLastName(),
                null,
                null
        );

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) user.setLastName(req.getLastName());
        if (req.getAvatar()    != null) user.setAvatar(req.getAvatar());
        if (req.getGroup()     != null) user.setGroup(req.getGroup());

        if (req.getGender()      != null) user.setGender(req.getGender());
        if (req.getBirthDate()   != null) user.setBirthDate(req.getBirthDate());
        if (req.getBirthPlace()  != null) user.setBirthPlace(req.getBirthPlace());
        if (req.getNationality() != null) user.setNationality(req.getNationality());

        if (req.getPhone()      != null) user.setPhone(req.getPhone());
        if (req.getAddress()    != null) user.setAddress(req.getAddress());
        if (req.getPostalCode() != null) user.setPostalCode(req.getPostalCode());
        if (req.getCity()       != null) user.setCity(req.getCity());
        if (req.getCountry()    != null) user.setCountry(req.getCountry());

        if (req.getEmergencyContacts() != null) user.setEmergencyContacts(req.getEmergencyContacts());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return toUserResponse(user);
    }

    public PageResponse<UserResponse> getUsers(
            int page, int size, String role, String group) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<User> usersPage;

        if (role != null && group != null) {
            usersPage = userRepository.findByDeletedFalseAndRoleAndGroup(
                    Role.valueOf(role), group, pageable);
        } else if (role != null) {
            usersPage = userRepository.findByDeletedFalseAndRole(
                    Role.valueOf(role), pageable);
        } else if (group != null) {
            usersPage = userRepository.findByDeletedFalseAndGroup(
                    group, pageable);
        } else {
            usersPage = userRepository.findByDeletedFalse(pageable);
        }

        return PageResponse.<UserResponse>builder()
                .content(usersPage.getContent().stream()
                        .map(this::toUserResponse).collect(Collectors.toList()))
                .page(page)
                .size(size)
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .build();
    }

    public UserResponse getUserById(String id, Jwt jwt) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", 404));

        String tokenKeycloakId = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        boolean isAdmin = roles != null && roles.contains("admin");
        boolean isSelf = user.getKeycloakId().equals(tokenKeycloakId);

        if (!isAdmin && !isSelf)
            throw new AppException("Access denied", 403);

        if (user.isDeleted() && !isAdmin)
            throw new AppException("User not found", 404);

        return toUserResponse(user);
    }

    public void softDelete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", 404));
        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        keycloakService.disableUser(user.getKeycloakId());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
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
                .deleted(user.isDeleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
