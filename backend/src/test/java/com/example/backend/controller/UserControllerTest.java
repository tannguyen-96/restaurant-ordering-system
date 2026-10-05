package com.example.backend.controller;

import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.UserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getUsersReturnsUsersFromService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getAllUsers()).thenReturn(List.of(user(userId)));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserByIdReturnsUserFromService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenReturn(user(userId));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(userService).getUserById(userId);
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.createUser(any())).thenReturn(user(userId));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"admin\"," +
                                "\"email\":\"admin@example.com\"," +
                                "\"password\":\"admin\"," +
                                "\"status\":\"active\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(userService).createUser(any());
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.updateUser(eq(userId), any())).thenReturn(user(userId));

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"updated\"," +
                                "\"email\":\"updated@example.com\"," +
                                "\"password\":\"updated-password\"," +
                                "\"status\":\"active\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(userService).updateUser(eq(userId), any());
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(userService).deleteUser(userId);
    }

    private UserResponse user(UUID userId) {
        return UserResponse.builder()
                .id(userId)
                .username("admin")
                .email("admin@example.com")
                .status("active")
                .build();
    }
}
