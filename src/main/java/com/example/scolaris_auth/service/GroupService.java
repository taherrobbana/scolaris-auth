package com.example.scolaris_auth.service;

import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.Group;
import com.example.scolaris_auth.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final KeycloakService keycloakService;
    private final GroupRepository groupRepository;

    public Group createGroup(String name) {
        if (groupRepository.existsByName(name))
            throw new AppException("Group already exists", 409);

        String keycloakGroupId = keycloakService.createKeycloakGroup(name);

        Group group = Group.builder()
                .keycloakGroupId(keycloakGroupId)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();

        return groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }
}