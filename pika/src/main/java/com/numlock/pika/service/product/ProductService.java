package com.numlock.pika.service.product;

import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(int productId);
    void registerProduct(ProductRegisterDto productRegisterDto, Principal principal, List<MultipartFile> images);
    ProductDetailDto getProductDetailById(int id, Principal principal);
}
