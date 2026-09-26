package com.example.backend.service.impl;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.backend.dto.request.BranchRequest;
import com.example.backend.dto.response.BranchResponse;
import com.example.backend.service.BranchService;

import com.example.backend.model.Branch;
import com.example.backend.repository.BranchRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    // GET all branches
    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAllActiveBranches().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET one branch
    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));
        return mapToResponse(branch);
    }

    // POST create branch
    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .status(request.getStatus())
                .domain(request.getDomain())
                .build();

        Branch savedBranch = branchRepository.save(branch);

        return mapToResponse(savedBranch);
    }

    // PUT update branch
    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setStatus(defaultValue(request.getStatus(), branch.getStatus()));
        branch.setDomain(defaultValue(request.getDomain(), branch.getDomain()));
        branch.setUpdatedAt(LocalDateTime.now());
        branch.setUpdatedBy("system");

        return mapToResponse(branchRepository.save(branch));
    }

    // DELETE branch
    @Override
    @Transactional
    public BranchResponse deleteBranch(UUID id) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));

        branch.setDeletedAt(LocalDateTime.now());
        Branch savedBranch = branchRepository.save(branch);
        return mapToResponse(savedBranch);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .status(branch.getStatus())
                .domain(branch.getDomain())
                .createdAt(branch.getCreatedAt().toString())
                .updatedAt(branch.getUpdatedAt().toString())
                .build();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
