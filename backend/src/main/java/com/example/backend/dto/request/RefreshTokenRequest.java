package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @Schema(description = "Refresh token returned by the login endpoint", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
