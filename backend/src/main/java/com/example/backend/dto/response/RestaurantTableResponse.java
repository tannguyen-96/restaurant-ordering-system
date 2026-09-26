package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RestaurantTableResponse {
    private UUID id;
    private String code;
    private UUID branchId;
    private String qrToken;
    private String status;
    private String createdAt;
    private String updatedAt;
}
