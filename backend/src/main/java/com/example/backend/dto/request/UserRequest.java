package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @Schema(description = "Unique username")
    @NotBlank(message = "Username must not be blank")
    private String username;

    @Schema(description = "User email")
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Plain text password, encoded before storage")
    @NotBlank(message = "Password must not be blank")
    private String password;

    @Schema(description = "User status")
    private String status;
}
