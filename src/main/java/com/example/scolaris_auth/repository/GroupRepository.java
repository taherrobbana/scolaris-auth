package com.example.scolaris_auth.repository;

import com.example.scolaris_auth.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByName(String name);
    boolean existsByName(String name);
    List<Group> findAll();
}