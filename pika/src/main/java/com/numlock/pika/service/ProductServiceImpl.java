package com.numlock.pika.service;

import com.numlock.pika.domain.Products;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(int productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        return ProductDto.fromEntity(product);
    }

    @Override
    public void createProduct(ProductDto productDto) {
        Products product = productDto.toEntity();
        productRepository.save(product);
    }
}
