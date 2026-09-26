package com.example.backend.service;
import java.util.List;
import java.util.UUID;

import com.example.backend.dto.response.BranchResponse;
import com.example.backend.dto.request.BranchRequest;

public interface BranchService {
    // GET all branches
    List<BranchResponse> getAllBranches();

    // GET one branch
    BranchResponse getBranchById(UUID id);

    // POST create branch
    BranchResponse createBranch(BranchRequest request);

    // PUT update branch
    BranchResponse updateBranch(UUID id, BranchRequest request);

    // DELETE branch
    BranchResponse deleteBranch(UUID id);
}
