package com.example.backend.repository;

import com.example.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = "SELECT * FROM restaurant_system.products WHERE delete_date IS NULL", nativeQuery = true)
    List<Product> findAllActiveProducts();

    @Query(value = "SELECT * FROM restaurant_system.products WHERE delete_date IS NULL AND id = :id", nativeQuery = true)
    List<Product> findOneActiveProduct(UUID id);
}
