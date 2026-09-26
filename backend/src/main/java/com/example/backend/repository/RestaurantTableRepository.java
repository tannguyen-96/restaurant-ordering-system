package com.example.backend.repository;

import com.example.backend.model.RestaurantTable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {
    @Query(value = "SELECT * FROM restaurant_system.tables WHERE deleted_at IS NULL", nativeQuery = true)
    List<RestaurantTable> findAllActiveTables(); 

    @Query(value = "SELECT * FROM restaurant_system.tables WHERE deleted_at IS NULL AND id = :id", nativeQuery = true)
    List<RestaurantTable> findOneActiveTable(UUID id);
}
