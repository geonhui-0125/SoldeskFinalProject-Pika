package com.numlock.pika.service.product;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.repository.CategoryRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    @Value("${file.upload-path}")
    private String uploadPath;

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
        // Repository의 findById가 int를 받도록 가정
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
        return ProductDto.fromEntity(product);
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
        }catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
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