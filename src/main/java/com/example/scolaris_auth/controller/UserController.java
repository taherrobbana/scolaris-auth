package com.example.scolaris_auth.controller;

import com.example.scolaris_auth.dto.request.AdminUpdateRequest;
import com.example.scolaris_auth.dto.request.BulkUserRequest;
import com.example.scolaris_auth.dto.request.UpdateSelfRequest;
import com.example.scolaris_auth.dto.response.PageResponse;
import com.example.scolaris_auth.dto.response.UserResponse;
import com.example.scolaris_auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, Object>> bulkCreate(
            @Valid @RequestBody List<BulkUserRequest> requests) {
        return ResponseEntity.status(201).body(userService.bulkCreate(requests));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateSelf(
            @RequestBody UpdateSelfRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.updateSelf(jwt.getSubject(), req));
    }

    @PutMapping("/admin/batch")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<UserResponse>> adminUpdate(
            @RequestBody Map<String, AdminUpdateRequest> updates) {
        return ResponseEntity.ok(userService.adminUpdate(updates));
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String groupe) {
        return ResponseEntity.ok(userService.getUsers(page, size, role, groupe));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserById(id, jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, String>> softDelete(@PathVariable String id) {
        userService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }
}