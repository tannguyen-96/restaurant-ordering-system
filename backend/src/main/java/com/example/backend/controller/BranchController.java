package com.example.backend.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;

import com.example.backend.dto.request.BranchRequest;
import com.example.backend.dto.response.BranchResponse;
import com.example.backend.service.BranchService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Controller", description = "APIs for managing branches") 
public class BranchController {
    private final BranchService branchService;

    // GET all branches
    @GetMapping
    @Operation(summary = "Get all branches", description = "Returns a list of all existing branches")
    public ResponseEntity<List<BranchResponse>> getBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    // GET one branch
    @GetMapping("/{id}")
    @Operation(summary = "Get branch details", description = "Retrieves branch information based on the provided identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<BranchResponse> getBranchById(
            @Parameter(description = "Unique ID of the branch", example = "5b672e7b-13be-496f-ac87-7b101d2e381b")
            @PathVariable UUID id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(response);
    }

    // POST create branch
    @PostMapping
    @Operation(summary = "Create a new branch", description = "Accepts branch details and initializes a new branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Branch created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Validation Error)")
    })
    public ResponseEntity<BranchResponse> createBranch(@Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // PUT update branch
    @PutMapping("/{id}")
    @Operation(summary = "Update a branch", description = "Updates the details of an existing branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Branch updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Validation Error)"),
            @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<BranchResponse> updateBranch(
            @Parameter(description = "Unique ID of the branch", example = "5b672e7b-13be-496f-ac87-7b101d2e381b")
            @PathVariable UUID id,
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(branchService.updateBranch(id, request));
    }

    // DELETE branch
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a branch", description = "Deletes an existing branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Branch deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<Void> deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
