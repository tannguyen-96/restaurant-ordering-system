package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @Schema(description = "Product Identifier", example = "PROD-001")
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    @Schema(description = "Quantity of the product", example = "2")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @Schema(description = "Price per unit", example = "150.00")
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
}