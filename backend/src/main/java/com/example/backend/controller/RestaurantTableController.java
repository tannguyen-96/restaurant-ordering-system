package com.example.backend.controller;

import com.example.backend.dto.request.RestaurantTableRequest;
import com.example.backend.dto.response.RestaurantTableResponse;
import com.example.backend.service.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/tables")
@RequiredArgsConstructor
@Tag(name = "Table Controller", description = "APIs for managing restaurant tables")
public class RestaurantTableController {
    private final RestaurantTableService tableService;

    // GET all tables
    @GetMapping
    @Operation(summary = "Get all tables", description = "Returns a list of all existing tables")
    public ResponseEntity<List<RestaurantTableResponse>> getTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    // GET one table
    @GetMapping("/{id}")
    @Operation(summary = "Get table details", description = "Retrieves table information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    public ResponseEntity<RestaurantTableResponse> getTableById(
            @Parameter(description = "Unique ID of the table")
            @PathVariable UUID id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    // POST create table
    @PostMapping
    @Operation(summary = "Create a new table", description = "Creates a table for a branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Table created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Validation Error)"),
            @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<RestaurantTableResponse> createTable(
            @Valid @RequestBody RestaurantTableRequest request) {
        return new ResponseEntity<>(tableService.createTable(request), HttpStatus.CREATED);
    }

    // PUT update table
    @PutMapping("/{id}")
    @Operation(summary = "Update a table", description = "Updates an existing table")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Table updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Validation Error)"),
            @ApiResponse(responseCode = "404", description = "Table or branch not found")
    })
    public ResponseEntity<RestaurantTableResponse> updateTable(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantTableRequest request) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    // DELETE table
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a table", description = "Deletes an existing table")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Table deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    public ResponseEntity<Void> deleteTable(@PathVariable UUID id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
