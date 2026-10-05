package com.example.backend.repository;

import com.example.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("select distinct role from Role role left join fetch role.policies where role.deleteDate is null")
    List<Role> findAllActiveRoles();

    @Query("select distinct role from Role role left join fetch role.policies where role.id = :id and role.deleteDate is null")
    List<Role> findOneActiveRole(UUID id);
}
