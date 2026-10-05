package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private String status;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
