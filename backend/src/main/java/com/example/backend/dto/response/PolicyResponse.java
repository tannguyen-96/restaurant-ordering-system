package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PolicyResponse {
    private UUID id;
    private String name;
    private String service;
    private String action;
}
