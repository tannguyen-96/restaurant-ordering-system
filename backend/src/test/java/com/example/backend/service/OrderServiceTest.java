package com.example.backend.service;

import com.example.backend.dto.request.OrderItemRequest;
import com.example.backend.dto.request.OrderRequest;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.model.Order;
import com.example.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderCalculatesTotalAndBindsItemsToOrder() {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item("PROD-1", 2, "10.50"), item("PROD-2", 1, "5.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order savedOrder = captor.getValue();
        assertThat(savedOrder.getOrderNumber()).isNotBlank();
        assertThat(savedOrder.getStatus()).isEqualTo("PENDING");
        assertThat(savedOrder.getCreatedAt()).isNotNull();
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("26.00");
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderItems()).allSatisfy(orderItem ->
                assertThat(orderItem.getOrder()).isSameAs(savedOrder));
        assertThat(response.getTotalAmount()).isEqualByComparingTo("26.00");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getOrderByIdMapsFoundOrder() {
        Long id = 7L;
        Order order = order(id, "ORD-7", "PENDING", "12.00");
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-7");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("12.00");
        assertThat(response.getCreatedAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    void getOrderByIdThrowsWhenOrderDoesNotExist() {
        Long id = 99L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found with ID: " + id);
    }

    @Test
    void getAllOrdersMapsRepositoryResults() {
        Order first = order(1L, "ORD-1", "PENDING", "10.00");
        Order second = order(2L, "ORD-2", "COMPLETED", "20.00");
        when(orderRepository.findAll()).thenReturn(List.of(first, second));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertThat(responses).extracting(OrderResponse::getOrderNumber)
                .containsExactly("ORD-1", "ORD-2");
        verify(orderRepository).findAll();
    }

    private OrderItemRequest item(String productId, int quantity, String price) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(new BigDecimal(price));
        return item;
    }

    private Order order(Long id, String orderNumber, String status, String totalAmount) {
        return Order.builder()
                .id(id)
                .orderNumber(orderNumber)
                .status(status)
                .totalAmount(new BigDecimal(totalAmount))
                .createdAt(LocalDateTime.of(2026, 10, 3, 12, 0))
                .build();
    }
}
