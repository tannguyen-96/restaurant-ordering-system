package com.example.backend.dto.response;
import lombok.Data;
import lombok.Builder;
import java.util.UUID;

@Data
@Builder
public class BranchResponse {
    private UUID id;
    private String name;
    private String address;
    private String status;
    private String domain;
    private String createdAt;
    private String updatedAt;
}
