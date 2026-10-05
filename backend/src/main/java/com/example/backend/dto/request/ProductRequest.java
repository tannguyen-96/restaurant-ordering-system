package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductRequest {

    @Schema(description = "Product name")
    @NotEmpty(message = "Product name must not be empty")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Product type")
    private String type;

    @Schema(description = "Product status")
    private String status;

    @Schema(description = "S3 image URL")
    private String image;
}
