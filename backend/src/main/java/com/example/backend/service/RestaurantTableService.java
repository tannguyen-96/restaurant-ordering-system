package com.example.backend.service;

import com.example.backend.dto.request.RestaurantTableRequest;
import com.example.backend.dto.response.RestaurantTableResponse;

import java.util.List;
import java.util.UUID;

public interface RestaurantTableService {
    // GET all tables
    List<RestaurantTableResponse> getAllTables();

    // GET one table
    RestaurantTableResponse getTableById(UUID id);

    // POST create table
    RestaurantTableResponse createTable(RestaurantTableRequest request);

    // PUT update table
    RestaurantTableResponse updateTable(UUID id, RestaurantTableRequest request);

    // DELETE table
    void deleteTable(UUID id);
}
