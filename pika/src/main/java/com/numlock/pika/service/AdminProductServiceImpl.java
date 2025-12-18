package com.numlock.pika.service;

import com.numlock.pika.domain.Products;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsForAdmin() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .map(ProductDto::fromEntity) // ProductDto에 fromEntity 메서드가 있다고 가정
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProductByAdmin(int productId, String reason) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        // product.setPolicyViolationStatus("DELETED_BY_ADMIN");
        // product.setPolicyViolationReason(reason);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void markProductAsViolatingPolicy(int productId, String reason) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        // 예를 들어, "REPORTED" 또는 "SUSPENDED" 등으로 상태 설정
        // product.setPolicyViolationStatus("REPORTED"); // 또는 SUSPENDED
        // product.setPolicyViolationReason(reason);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void liftProductViolation(int productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        // product.setPolicyViolationStatus("NORMAL");
        // product.setPolicyViolationReason(null);
        productRepository.save(product);
    }
}
