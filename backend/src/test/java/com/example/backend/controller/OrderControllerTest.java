package com.example.backend.controller;

import com.example.backend.dto.response.OrderResponse;
import com.example.backend.service.OrderService;
import com.example.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        when(orderService.createOrder(any())).thenReturn(order(7L, "ORD-7"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"items\":[{" +
                                "\"productId\":\"PROD-1\"," +
                                "\"quantity\":2," +
                                "\"price\":10.50" +
                                "}]" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.orderNumber").value("ORD-7"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(21.00));

        verify(orderService).createOrder(any());
    }

    @Test
    void getOrderByIdReturnsOrderFromService() throws Exception {
        when(orderService.getOrderById(7L)).thenReturn(order(7L, "ORD-7"));

        mockMvc.perform(get("/api/v1/orders/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.orderNumber").value("ORD-7"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrderById(7L);
    }

    @Test
    void getAllOrdersReturnsOrdersFromService() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(order(7L, "ORD-7"), order(8L, "ORD-8")));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-7"))
                .andExpect(jsonPath("$[1].id").value(8))
                .andExpect(jsonPath("$[1].orderNumber").value("ORD-8"));

        verify(orderService).getAllOrders();
    }

    private OrderResponse order(Long id, String orderNumber) {
        return OrderResponse.builder()
                .id(id)
                .orderNumber(orderNumber)
                .status("PENDING")
                .totalAmount(new BigDecimal("21.00"))
                .createdAt(LocalDateTime.of(2026, 10, 3, 12, 0))
                .build();
    }
}
