package com.example.backend.service;

import com.example.backend.dto.request.BranchRequest;
import com.example.backend.dto.response.BranchResponse;
import com.example.backend.helper.StringHelper;
import com.example.backend.model.Branch;
import com.example.backend.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAllActiveBranches().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .status(request.getStatus())
                .domain(request.getDomain())
                .build();

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setStatus(StringHelper.defaultValue(request.getStatus(), branch.getStatus()));
        branch.setDomain(StringHelper.defaultValue(request.getDomain(), branch.getDomain()));
        branch.setUpdatedAt(LocalDateTime.now());
        branch.setUpdatedBy("system");

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse deleteBranch(UUID id) {
        Branch branch = branchRepository.findOneActiveBranch(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));

        branch.setDeletedAt(LocalDateTime.now());
        return mapToResponse(branchRepository.save(branch));
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
}
