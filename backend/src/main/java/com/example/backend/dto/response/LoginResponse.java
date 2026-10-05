package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data 
public class LoginResponse {
    @Schema(description = "Authenticated username", example = "admin")
    private String username;

    @Schema(description = "Names of roles assigned to the user")
    private List<String> roleNames;

    @Schema(description = "Short-lived JWT access token")
    private String accessToken;

    @Schema(description = "Rotating refresh token")
    private String refreshToken;

    @Schema(description = "Message")
    private String message;
}
