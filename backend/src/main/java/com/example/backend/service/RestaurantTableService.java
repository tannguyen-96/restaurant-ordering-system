package com.example.backend.service;

import com.example.backend.dto.request.RestaurantTableRequest;
import com.example.backend.dto.response.RestaurantTableResponse;
import com.example.backend.helper.ShortCode;
import com.example.backend.model.Branch;
import com.example.backend.model.RestaurantTable;
import com.example.backend.repository.BranchRepository;
import com.example.backend.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private static final int MAX_RETRIES = 5;

    private final RestaurantTableRepository tableRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> getAllTables() {
        return tableRepository.findAllActiveTables().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantTableResponse getTableById(UUID id) {
        return mapToResponse(findTable(id));
    }

    @Transactional
    public RestaurantTableResponse createTable(RestaurantTableRequest request) {
        Branch branch = findBranch(request.getBranchId());
        RestaurantTable table = saveWithUniqueToken(RestaurantTable.builder()
                .code(request.getCode())
                .branch(branch)
                .status(request.getStatus())
                .build());

        return mapToResponse(table);
    }

    @Transactional
    public RestaurantTableResponse updateTable(UUID id, RestaurantTableRequest request) {
        RestaurantTable table = findTable(id);
        Branch branch = findBranch(request.getBranchId());

        table.setCode(request.getCode());
        table.setBranch(branch);
        table.setStatus(request.getStatus());

        return mapToResponse(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(UUID id) {
        tableRepository.delete(findTable(id));
    }

    private RestaurantTable saveWithUniqueToken(RestaurantTable table) {
        int attempts = 0;
        while (true) {
            table.setQrToken(ShortCode.generate(10));
            try {
                return tableRepository.save(table);
            } catch (DataIntegrityViolationException exception) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new IllegalStateException(
                            "Failed to generate a unique QR token after " + MAX_RETRIES + " attempts", exception);
                }
            }
        }
    }

    private RestaurantTable findTable(UUID id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found with ID: " + id));
    }

    private Branch findBranch(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + id));
    }

    private RestaurantTableResponse mapToResponse(RestaurantTable table) {
        return RestaurantTableResponse.builder()
                .id(table.getId())
                .code(table.getCode())
                .branchId(table.getBranch().getId())
                .qrToken(table.getQrToken())
                .status(table.getStatus())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .build();
    }
}
