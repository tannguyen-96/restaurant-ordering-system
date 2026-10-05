package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoleRequest {

    @Schema(description = "Unique role name", example = "manager")
    @NotBlank(message = "Role name must not be blank")
    private String name;

    @Schema(description = "Role description")
    private String description;

    @Schema(description = "Role status", example = "active")
    private String status;

    @Schema(description = "Policy IDs assigned to the role")
    @NotEmpty(message = "At least one policy is required")
    private List<UUID> policyIds;
}
