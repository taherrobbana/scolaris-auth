package com.example.scolaris_auth.repository;

import com.example.scolaris_auth.model.User;
import com.example.scolaris_auth.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByKeycloakId(String keycloakId);
    boolean existsByUsername(String username);

    // Filtres pour liste paginée
    Page<User> findByDeletedFalseAndRole(Role role, Pageable pageable);
    Page<User> findByDeletedFalseAndGroupe(String groupe, Pageable pageable);
    Page<User> findByDeletedFalseAndRoleAndGroupe(Role role, String groupe, Pageable pageable);
    Page<User> findByDeletedFalse(Pageable pageable);
}