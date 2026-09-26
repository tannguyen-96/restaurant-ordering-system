package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RestaurantTableRequest {
    @Schema(description = "Table code")
    @NotEmpty(message = "Table code must not be empty")
    private String code;

    @Schema(description = "Branch ID")
    @NotNull(message = "Branch ID must not be null")
    private UUID branchId;

    @Schema(description = "Table status")
    private String status;
}
