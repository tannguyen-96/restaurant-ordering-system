package com.example.backend.repository;

import com.example.backend.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    List<Policy> findAllByIdIn(List<UUID> ids);
}
