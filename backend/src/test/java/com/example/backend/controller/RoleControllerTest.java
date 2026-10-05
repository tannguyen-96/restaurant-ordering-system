package com.example.backend.controller;

import com.example.backend.dto.response.RoleResponse;
import com.example.backend.service.JwtService;
import com.example.backend.service.RoleService;
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

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getRolesReturnsRolesFromService() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(roleService.getAllRoles()).thenReturn(List.of(role(roleId)));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(roleId.toString()))
                .andExpect(jsonPath("$[0].name").value("manager"));

        verify(roleService).getAllRoles();
    }

    @Test
    void getRoleByIdReturnsRoleFromService() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(roleService.getRoleById(roleId)).thenReturn(role(roleId));

        mockMvc.perform(get("/api/v1/roles/{id}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("manager"));

        verify(roleService).getRoleById(roleId);
    }

    @Test
    void createRoleRequiresPolicyIdsAndReturnsCreatedRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        when(roleService.createRole(any())).thenReturn(role(roleId));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"manager\"," +
                                "\"description\":\"Manager role\"," +
                                "\"policyIds\":[\"" + policyId + "\"]" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("manager"));

        verify(roleService).createRole(any());
    }

    @Test
    void updateRoleReturnsUpdatedRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        when(roleService.updateRole(eq(roleId), any())).thenReturn(role(roleId));

        mockMvc.perform(put("/api/v1/roles/{id}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"manager\"," +
                                "\"description\":\"Updated role\"," +
                                "\"policyIds\":[\"" + policyId + "\"]" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("manager"));

        verify(roleService).updateRole(eq(roleId), any());
    }

    @Test
    void deleteRoleReturnsNoContent() throws Exception {
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/roles/{id}", roleId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(roleService).deleteRole(roleId);
    }

    private RoleResponse role(UUID id) {
        return RoleResponse.builder()
                .id(id)
                .name("manager")
                .description("Manager role")
                .status("active")
                .policies(List.of())
                .build();
    }
}
