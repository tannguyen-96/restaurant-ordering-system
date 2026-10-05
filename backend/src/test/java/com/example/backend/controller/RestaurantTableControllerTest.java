package com.example.backend.controller;

import com.example.backend.dto.response.RestaurantTableResponse;
import com.example.backend.service.RestaurantTableService;
import com.example.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(RestaurantTableController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantTableService tableService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getTablesReturnsTablesFromService() throws Exception {
        UUID tableId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        when(tableService.getAllTables()).thenReturn(List.of(table(tableId, branchId)));

        mockMvc.perform(get("/api/v1/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(tableId.toString()))
                .andExpect(jsonPath("$[0].code").value("T-01"))
                .andExpect(jsonPath("$[0].branchId").value(branchId.toString()))
                .andExpect(jsonPath("$[0].qrToken").value("QR-TOKEN"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(tableService).getAllTables();
    }

    @Test
    void getTableByIdReturnsTableFromService() throws Exception {
        UUID tableId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        when(tableService.getTableById(tableId)).thenReturn(table(tableId, branchId));

        mockMvc.perform(get("/api/v1/tables/{id}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tableId.toString()))
                .andExpect(jsonPath("$.code").value("T-01"))
                .andExpect(jsonPath("$.branchId").value(branchId.toString()));

        verify(tableService).getTableById(tableId);
    }

    @Test
    void createTableReturnsCreatedTable() throws Exception {
        UUID branchId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        when(tableService.createTable(any())).thenReturn(table(tableId, branchId));

        mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"T-01\"," +
                                "\"branchId\":\"" + branchId + "\"," +
                                "\"status\":\"AVAILABLE\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tableId.toString()))
                .andExpect(jsonPath("$.code").value("T-01"));

        verify(tableService).createTable(any());
    }

    @Test
    void updateTableReturnsUpdatedTable() throws Exception {
        UUID tableId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        when(tableService.updateTable(eq(tableId), any())).thenReturn(table(tableId, branchId));

        mockMvc.perform(put("/api/v1/tables/{id}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"T-02\"," +
                                "\"branchId\":\"" + branchId + "\"," +
                                "\"status\":\"OCCUPIED\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tableId.toString()))
                .andExpect(jsonPath("$.code").value("T-01"));

        verify(tableService).updateTable(eq(tableId), any());
    }

    @Test
    void deleteTableReturnsNoContent() throws Exception {
        UUID tableId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tables/{id}", tableId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(tableService).deleteTable(tableId);
    }

    private RestaurantTableResponse table(UUID tableId, UUID branchId) {
        return RestaurantTableResponse.builder()
                .id(tableId)
                .code("T-01")
                .branchId(branchId)
                .qrToken("QR-TOKEN")
                .status("AVAILABLE")
                .build();
    }
}
