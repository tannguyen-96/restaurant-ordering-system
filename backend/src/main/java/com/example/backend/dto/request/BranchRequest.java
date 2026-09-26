package com.example.backend.dto.request;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data
public class BranchRequest {
    @Schema(description = "Branch name")
    @NotEmpty(message = "Branch name must not be empty")
    private String name;

    @Schema(description = "Branch address")
    @NotEmpty(message = "Branch address must not be empty")
    private String address;

    @Schema(description = "Branch status")
    private String status;

    @Schema(description = "Branch domain")
    private String domain;
}
