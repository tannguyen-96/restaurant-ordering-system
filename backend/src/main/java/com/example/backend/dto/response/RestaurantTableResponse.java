package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
public class RestaurantTableResponse {
    private UUID id;
    private String code;
    private UUID branchId;
    private String qrToken;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
