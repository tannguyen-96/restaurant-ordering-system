package com.example.backend.service;

import com.example.backend.dto.request.ProductRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.helper.StringHelper;
import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findOneActiveProduct(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .status(StringHelper.defaultValue(request.getStatus(), "active"))
                .image(request.getImage())
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findOneActiveProduct(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setType(request.getType());
        product.setStatus(StringHelper.defaultValue(request.getStatus(), product.getStatus()));
        product.setImage(request.getImage());
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy("system");

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse deleteProduct(UUID id) {
        Product product = productRepository.findOneActiveProduct(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        product.setDeleteDate(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .type(product.getType())
                .status(product.getStatus())
                .image(product.getImage())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
