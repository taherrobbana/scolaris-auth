package com.example.scolaris_auth.service;

import com.example.scolaris_auth.dto.response.GroupResponse;
import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.Group;
import com.example.scolaris_auth.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final KeycloakService keycloakService;
    private final GroupRepository groupRepository;

    public GroupResponse createGroup(String name) {
        if (groupRepository.existsByName(name))
            throw new AppException("Group already exists", 409);

        String keycloakGroupId = keycloakService.createKeycloakGroup(name);

        Group group = Group.builder()
                .keycloakGroupId(keycloakGroupId)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();

        return toGroupResponse(groupRepository.save(group));
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    public GroupResponse updateGroup(String id, String newName) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException("Group not found", 404));

        if (groupRepository.existsByName(newName) && !group.getName().equals(newName))
            throw new AppException("Group name already exists", 409);

        keycloakService.updateKeycloakGroup(group.getKeycloakGroupId(), newName);

        group.setName(newName);
        return toGroupResponse(groupRepository.save(group));
    }

    public void deleteGroup(String id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException("Group not found", 404));

        keycloakService.deleteKeycloakGroup(group.getKeycloakGroupId());
        groupRepository.delete(group);
    }

    private GroupResponse toGroupResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .keycloakGroupId(group.getKeycloakGroupId())
                .createdAt(group.getCreatedAt())
                .build();
    }
}