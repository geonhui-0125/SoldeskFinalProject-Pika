package com.numlock.pika.service.product;

import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ProductService {

    List<ProductDto> getAllProducts();


    ProductDto getProductById(int productId);

    // Page<ProductDto> getProductList(Pageable pageable); // 현재 DB 연동이 아니므로 임시 주석 처리

    void registerProduct(ProductRegisterDto productRegisterDto, Principal principal, List<MultipartFile> images);

    List<String> getImageUrls(String folderUrl);
}