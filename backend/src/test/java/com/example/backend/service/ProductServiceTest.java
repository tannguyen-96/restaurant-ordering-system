package com.example.backend.service;

import com.example.backend.dto.request.ProductRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProductsMapsActiveProducts() {
        Product product = product("Burger", "Food", "ACTIVE", "https://s3.example.com/burger.jpg");
        when(productRepository.findAllActiveProducts()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(product.getId());
            assertThat(response.getName()).isEqualTo("Burger");
            assertThat(response.getDescription()).isEqualTo("Food");
            assertThat(response.getType()).isEqualTo("Food");
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getImage()).isEqualTo("https://s3.example.com/burger.jpg");
        });
    }

    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findOneActiveProduct(id)).thenReturn(List.of());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found with ID: " + id);
    }

    @Test
    void createProductBuildsProductWithActiveDefaultStatus() {
        ProductRequest request = request("Burger", "Food", null, "https://s3.example.com/burger.jpg");
        Product savedProduct = product("Burger", "Food", "active", "https://s3.example.com/burger.jpg");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Burger");
        assertThat(captor.getValue().getStatus()).isEqualTo("active");
        assertThat(response.getId()).isEqualTo(savedProduct.getId());
    }

    @Test
    void updateProductReplacesProductFieldsAndPreservesStatusWhenBlank() {
        UUID id = UUID.randomUUID();
        Product product = product("Old Burger", "Old description", "ACTIVE", "old-image.jpg");
        product.setId(id);
        ProductRequest request = request("New Burger", "New description", " ", "new-image.jpg");
        when(productRepository.findOneActiveProduct(id)).thenReturn(List.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductResponse response = productService.updateProduct(id, request);

        assertThat(product.getName()).isEqualTo("New Burger");
        assertThat(product.getDescription()).isEqualTo("New description");
        assertThat(product.getType()).isEqualTo("Food");
        assertThat(product.getStatus()).isEqualTo("ACTIVE");
        assertThat(product.getImage()).isEqualTo("new-image.jpg");
        assertThat(product.getUpdatedAt()).isNotNull();
        assertThat(product.getUpdatedBy()).isEqualTo("system");
        assertThat(response.getName()).isEqualTo("New Burger");
    }

    @Test
    void deleteProductSetsDeleteDateAndSavesProduct() {
        UUID id = UUID.randomUUID();
        Product product = product("Burger", "Food", "ACTIVE", "burger.jpg");
        product.setId(id);
        when(productRepository.findOneActiveProduct(id)).thenReturn(List.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductResponse response = productService.deleteProduct(id);

        assertThat(product.getDeleteDate()).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        verify(productRepository).save(product);
    }

    private ProductRequest request(String name, String description, String status, String image) {
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setType("Food");
        request.setStatus(status);
        request.setImage(image);
        return request;
    }

    private Product product(String name, String type, String status, String image) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 3, 12, 0);
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(type)
                .type(type)
                .status(status)
                .image(image)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
