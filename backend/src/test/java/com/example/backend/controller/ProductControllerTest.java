package com.example.backend.controller;

import com.example.backend.dto.response.ProductResponse;
import com.example.backend.service.ProductService;
import com.example.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getProductsReturnsProductsFromService() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getAllProducts()).thenReturn(List.of(product(productId)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Burger"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].image").value("https://s3.example.com/burger.jpg"));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductByIdReturnsProductFromService() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId)).thenReturn(product(productId));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(productService).getProductById(productId);
    }

    @Test
    void createProductReturnsCreatedProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.createProduct(any())).thenReturn(product(productId));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Burger\"," +
                                "\"description\":\"Food\"," +
                                "\"type\":\"Food\"," +
                                "\"status\":\"ACTIVE\"," +
                                "\"image\":\"https://s3.example.com/burger.jpg\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(productService).createProduct(any());
    }

    @Test
    void updateProductReturnsUpdatedProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.updateProduct(eq(productId), any())).thenReturn(product(productId));

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"Updated Burger\"," +
                                "\"description\":\"Updated food\"," +
                                "\"type\":\"Food\"," +
                                "\"status\":\"ACTIVE\"," +
                                "\"image\":\"https://s3.example.com/updated.jpg\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(productService).updateProduct(eq(productId), any());
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(productService).deleteProduct(productId);
    }

    private ProductResponse product(UUID productId) {
        return ProductResponse.builder()
                .id(productId)
                .name("Burger")
                .description("Food")
                .type("Food")
                .status("ACTIVE")
                .image("https://s3.example.com/burger.jpg")
                .createdAt(LocalDateTime.of(2026, 10, 3, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 10, 3, 12, 0))
                .build();
    }
}
