package com.numlock.pika.service;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.CategoryRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

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
        Users seller = userRepository.findById(productDto.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("판매자 정보를 찾을 수 없습니다."));

        Categories category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리 정보를 찾을 수 없습니다."));

        Products products = Products.builder()
                .productId(productDto.getProductId())
                .seller(seller)
                .category(category)
                .price(productDto.getPrice())
                .title(productDto.getTitle())
                .description(productDto.getDescription())
                .productImage(productDto.getProductImage())
                .viewCnt(productDto.getViewCnt())
                .createdAt(productDto.getCreatedAt())
                .productState(productDto.getProductState())
                .build();

        productRepository.save(products);
    }
}
