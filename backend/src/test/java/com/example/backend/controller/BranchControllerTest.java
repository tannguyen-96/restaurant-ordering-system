package com.example.backend.controller;

import com.example.backend.dto.response.BranchResponse;
import com.example.backend.service.BranchService;
import com.example.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BranchController.class)
@AutoConfigureMockMvc(addFilters = false)
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getBranchesReturnsBranchesFromService() throws Exception {
        UUID branchId = UUID.randomUUID();
        BranchResponse branch = BranchResponse.builder()
                .id(branchId)
                .name("Downtown")
                .address("1 Main Street")
                .status("ACTIVE")
                .domain("downtown.example.com")
                .build();
        when(branchService.getAllBranches()).thenReturn(List.of(branch));

        mockMvc.perform(get("/api/v1/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(branchId.toString()))
                .andExpect(jsonPath("$[0].name").value("Downtown"))
                .andExpect(jsonPath("$[0].address").value("1 Main Street"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].domain").value("downtown.example.com"));

        verify(branchService).getAllBranches();
    }

    @Test
    void getBranchByIdReturnsBranchFromService() throws Exception {
        UUID branchId = UUID.randomUUID();
        when(branchService.getBranchById(branchId)).thenReturn(branch(branchId));

        mockMvc.perform(get("/api/v1/branches/{id}", branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId.toString()))
                .andExpect(jsonPath("$.name").value("Downtown"))
                .andExpect(jsonPath("$.address").value("1 Main Street"));

        verify(branchService).getBranchById(branchId);
    }

    @Test
    void createBranchReturnsCreatedBranch() throws Exception {
        UUID branchId = UUID.randomUUID();
        when(branchService.createBranch(any())).thenReturn(branch(branchId));

        mockMvc.perform(post("/api/v1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Downtown\"," +
                                "\"address\":\"1 Main Street\"," +
                                "\"status\":\"ACTIVE\"," +
                                "\"domain\":\"downtown.example.com\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(branchId.toString()))
                .andExpect(jsonPath("$.name").value("Downtown"));

        verify(branchService).createBranch(any());
    }

    @Test
    void updateBranchReturnsUpdatedBranch() throws Exception {
        UUID branchId = UUID.randomUUID();
        when(branchService.updateBranch(eq(branchId), any())).thenReturn(branch(branchId));

        mockMvc.perform(put("/api/v1/branches/{id}", branchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Updated Downtown\"," +
                                "\"address\":\"2 Main Street\"," +
                                "\"status\":\"ACTIVE\"," +
                                "\"domain\":\"downtown.example.com\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId.toString()))
                .andExpect(jsonPath("$.name").value("Downtown"));

        verify(branchService).updateBranch(eq(branchId), any());
    }

    @Test
    void deleteBranchReturnsNoContent() throws Exception {
        UUID branchId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/branches/{id}", branchId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(branchService).deleteBranch(branchId);
    }

    private BranchResponse branch(UUID branchId) {
        return BranchResponse.builder()
                .id(branchId)
                .name("Downtown")
                .address("1 Main Street")
                .status("ACTIVE")
                .domain("downtown.example.com")
                .build();
    }
}
