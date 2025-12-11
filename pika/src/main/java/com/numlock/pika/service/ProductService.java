package com.numlock.pika.service;

import com.numlock.pika.dto.ProductDto;
import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(int productId);
    void createProduct(ProductDto productDto);
}
