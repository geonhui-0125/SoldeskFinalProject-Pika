package com.numlock.pika.service.product;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Reviews;
import com.numlock.pika.domain.Users;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
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
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        return ProductDto.fromEntity(product);
    }

    public ProductDetailDto getProductDetailById(int productId, Principal principal) {

        Products products = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));

        int favoriteCnt = favoriteProductRepository.countByProduct(products);

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

        // cateDepOne > cateDepTwo 문자열의 공백 제거(split로 분리후 앞뒤 결합)해서 데이터 조회
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

    public String saveImages(List<MultipartFile> images) throws IOException {
        // 상품 전용 폴더명
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

        // 👇 이미지 폴더 URL만 반환
        return "/upload/" + folderName + "/";
    }

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
