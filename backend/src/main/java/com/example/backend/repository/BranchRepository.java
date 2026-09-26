package com.example.backend.repository;
import com.example.backend.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    @Query(value = "SELECT * FROM restaurant_system.branches WHERE deleted_at IS NULL", nativeQuery = true)
    List<Branch> findAllActiveBranches(); 

    @Query(value = "SELECT * FROM restaurant_system.branches WHERE deleted_at IS NULL AND id = :id", nativeQuery = true)
    List<Branch> findOneActiveBranch(UUID id);
}
