package com.example.scolaris_auth.controller;

import com.example.scolaris_auth.dto.request.CreateGroupRequest;
import com.example.scolaris_auth.dto.response.GroupResponse;
import com.example.scolaris_auth.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest req) {
        return ResponseEntity.status(201)
                .body(groupService.createGroup(req.getName()));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody CreateGroupRequest req) {
        return ResponseEntity.ok(groupService.updateGroup(id, req.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}