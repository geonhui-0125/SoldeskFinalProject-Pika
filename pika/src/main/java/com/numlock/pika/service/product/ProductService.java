package com.numlock.pika.service.product;

import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ProductService {

    List<ProductDto> getAllProducts();

    void upView(int productId);

    ProductDto getProductById(int productId);

    Page<ProductDto> getProductList(Pageable pageable);

    Page<ProductDto> searchProducts(String keyword, String categoryName, Pageable pageable);

    void registerProduct(ProductRegisterDto productRegisterDto, Principal principal, List<MultipartFile> images);

    List<String> getImageUrls(String folderUrl);

    ProductDetailDto getProductDetailById(int id, Principal principal);
    List<ProductDto> getProductsBySeller(String sellerId);
    void updateProduct(int productId, ProductRegisterDto productDto, Principal principal);
    void deleteProduct(int productId, Principal principal);

}

