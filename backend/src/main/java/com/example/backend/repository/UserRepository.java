package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameAndDeleteDateIsNull(String username);

    @Query(value = "SELECT * FROM restaurant_system.users WHERE delete_date IS NULL", nativeQuery = true)
    List<User> findAllActiveUsers();

    @Query(value = "SELECT * FROM restaurant_system.users WHERE delete_date IS NULL AND id = :id", nativeQuery = true)
    List<User> findOneActiveUser(UUID id);
}
