package com.example.backend.dto.request;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;

public class BranchRequest {
    @Schema(description = "Branch name")
    @NotEmpty(message = "Branch name must not be empty")
    private String name;

    @Schema(description = "Branch address")
    @NotEmpty(message = "Branch address must not be empty")
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
