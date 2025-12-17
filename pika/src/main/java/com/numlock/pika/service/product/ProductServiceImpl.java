package com.numlock.pika.service.product;

import com.numlock.pika.domain.*;
import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.repository.*;
import jakarta.persistence.EntityNotFoundException;


import java.nio.file.Files;
import java.time.LocalDateTime;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.numlock.pika.dto.ProductDetailDto.calculateTimeAgo;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FavoriteProductRepository  favoriteProductRepository;
    private final ReviewRepository reviewRepository;

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
        // Repository의 findById가 int를 받도록 가정
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        return ProductDto.fromEntity(product);
    }


    public ProductDetailDto getProductDetailById(int productId, Principal principal) {

        Products products = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));

        int favoriteCnt = favoriteProductRepository.countByProduct(products);

        Users users = userRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보를 찾지 못했습니다."));

        boolean wished = favoriteProductRepository.existsByUserAndProduct(users, products);

        System.out.println("seller :" + products.getSeller().getId());
        List<Reviews> reviewsList = reviewRepository.findBySeller_Id(products.getSeller().getId());

        double star = 0;
        int sum = 0;
        int count = 0;

        for(Reviews reviews : reviewsList) {

            count ++;
            sum += reviews.getScore();

            star = (double) sum / count;
        }

        ProductDetailDto productDetailDto = ProductDetailDto.builder()
                .productId(productId)
                .sellerId(products.getSeller().getId())
                .seller(products.getSeller())
                .buyerId(principal.getName())
                .title(products.getTitle())
                .description(products.getDescription())
                .price(products.getPrice())
                .category(products.getCategory().getCategory())
                .favoriteCnt(favoriteCnt)
                .viewCnt(products.getViewCnt())
                .wished(wished)
                .timeAgo(calculateTimeAgo(products.getCreatedAt()))
                .star(star)
                .images(getImageUrls(products.getProductImage()))
                .build();

        if (productDetailDto.getCategory() != null && productDetailDto.getCategory().contains(">")) {
            productDetailDto.setCategoryOne(productDetailDto.getCategory().split(">")[0]);
            productDetailDto.setCategoryTwo(productDetailDto.getCategory().split(">")[1]);
        }

        return productDetailDto;
    }


    @Override
    public void registerProduct(ProductRegisterDto productRegisterDto, Principal principal, List<MultipartFile> images) {
        Users seller = userRepository.findById(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("판매자 정보를 찾을 수 없습니다."));

        String originCategory = productRegisterDto.getCategory();

        Categories category = categoryRepository.findByCategory(
                        originCategory.split(" > ")[0] + ">" + originCategory.split(" > ")[1])
                .orElseThrow(() -> new EntityNotFoundException("카테고리 정보를 찾을 수 없습니다."));

        try {
            String imagesPath = saveImages(images);

            Products products = Products.builder()
                    .seller(seller)
                    .category(category)
                    .price(productRegisterDto.getPrice())
                    .title(productRegisterDto.getTitle())
                    .description(productRegisterDto.getDescription())
                    .productImage(imagesPath)
                    .viewCnt(0)
                    .createdAt(LocalDateTime.now())
                    .productState(0)
                    .build();

            productRepository.save(products);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySeller(String sellerId) {
        return productRepository.findBySeller_Id(sellerId).stream()
                .map(product -> {
                    ProductDto dto = ProductDto.fromEntity(product);

                    String folderUrl = dto.getProductImage();

                    if (folderUrl != null && !folderUrl.isEmpty()) {
                        List<String> imageUrls = getImageUrls(folderUrl);

                        if (!imageUrls.isEmpty()) {
                            dto.setProductImage(imageUrls.get(0));
                        } else {
                            // 이미지가 없는 경우, 디폴트 이미지
                            dto.setProductImage(null);
                        }
                    } else {
                        // 폴더 URL 자체가 없는 경우 디폴트 이미지
                        dto.setProductImage(null);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateProduct(int productId, ProductRegisterDto dto, Principal principal) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getSeller().getId().equals(principal.getName())) {
            throw new SecurityException("Not authorized to update this product");
        }

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            String originCategory = dto.getCategory();
            // Assuming category format "Main > Sub"
            String[] parts = originCategory.split(" > ");
            if (parts.length == 2) {
                Categories category = categoryRepository.findByCategory(parts[0] + ">" + parts[1])
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
                product.setCategory(category);
            }
        }
    }

    @Override
    public void deleteProduct(int productId, Principal principal) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getSeller().getId().equals(principal.getName())) {
            throw new SecurityException("Not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    public String saveImages(List<MultipartFile> images) throws IOException {
        String folderName = UUID.randomUUID().toString();
        Path dirPath = Paths.get(uploadPath, folderName);

        Files.createDirectories(dirPath);

        for (MultipartFile image : images) {
            System.out.println("image 확인 : " + image.getOriginalFilename());

            if (image.isEmpty()) continue;

            String originalName = image.getOriginalFilename();

            String savedName = Paths.get(originalName).getFileName().toString();
            Path savePath = dirPath.resolve(savedName);

            image.transferTo(savePath.toFile());
        }

        return "/upload/" + folderName + "/";
    }

    @Override
    public List<String> getImageUrls(String folderUrl) {
        String folderName = folderUrl.replace("/upload/", "");

        Path dir = Paths.get(uploadPath, folderName);

        if (!Files.exists(dir)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(path -> folderUrl + path.getFileName().toString())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("이미지 목록 조회 실패", e);
        }
    }
}