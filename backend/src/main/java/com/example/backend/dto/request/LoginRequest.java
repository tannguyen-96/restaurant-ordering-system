package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "Username used for authentication", example = "admin")
    @NotBlank(message = "Username must not be blank")
    private String username;

    @Schema(description = "User password", example = "admin")
    @NotBlank(message = "Password must not be blank")
    private String password;
}