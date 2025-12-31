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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final PaymentRepository paymentRepository;
    private final MessageRepository messageRepository; // MessageRepository 주입
    private final com.numlock.pika.service.ReviewService reviewService; // ReviewService 주입

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductList(Pageable pageable) {

        return productRepository.findAll(pageable).map(product -> {
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
        });
    }

    @Override
    @Transactional
    public void upView(int productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품은 존재하지 않습니다. ID: " + productId));
        product.setViewCnt(product.getViewCnt() + 1);
        productRepository.save(product);
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

        Users users;
        boolean wished;
        String buyerId;
        String buyerType;
        String impUid = null;

        if (principal != null) {
            users = userRepository.findById(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보를 찾지 못했습니다."));
            wished = favoriteProductRepository.existsByUserAndProduct(users, products);
            buyerId = principal.getName();

            if (buyerId.equals(products.getSeller().getId())) {
                buyerType = "sell"; // 판매자
            } else {
                // 특정 상품과 구매자에 대한 결제 정보 확인
                Optional<Payments> paymentOpt = paymentRepository.findByBuyerAndTask(users, products);
                if (paymentOpt.isPresent()) {
                    buyerType = "buy"; // 해당 상품을 결제한 구매자
                    impUid = paymentOpt.get().getImpUid();
                } else {
                    buyerType = "view"; // 로그인했지만 구매/판매자 아닌 일반 사용자
                }
            }
        } else {
            buyerId = null;
            wished = false;
            buyerType = "visit"; // 비로그인 방문자
        }

        // 사용자가 로그인한 경우에만 hasReviewed 플래그를 확인합니다.
        boolean hasReviewed = false;
        if (principal != null) {
            hasReviewed = reviewService.hasUserReviewedProduct(principal.getName(), productId);
        }


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
                .buyerId(buyerId)
                .title(products.getTitle())
                .description(products.getDescription())
                .price(products.getPrice())
                .category(products.getCategory().getCategory())
                .favoriteCnt(favoriteCnt)
                .viewCnt(products.getViewCnt())
                .productStat(products.getProductState())
                .buyerType(buyerType)
                .wished(wished)
                .timeAgo(calculateTimeAgo(products.getCreatedAt()))
                .star(star)
                .images(getImageUrls(products.getProductImage()))
                .impUid(impUid) // impUid 추가
                .hasReviewed(hasReviewed) // hasReviewed 필드 추가
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

                    // 채팅 존재 여부 설정
                    boolean hasChat = messageRepository.existsByProduct_ProductId(product.getProductId());
                    dto.setHasChat(hasChat);

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

        if (product.getProductState() != 0) {
            throw new IllegalStateException("판매 중인 상품만 수정할 수 있습니다.");
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

        // 현재 로그인한 사용자 정보 조회
        Users currentUser = userRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ADMIN 역할이거나 해당 상품의 판매자인 경우에만 삭제 허용
        if ("ADMIN".equals(currentUser.getRole()) || product.getSeller().getId().equals(currentUser.getId())) {
            if (!"ADMIN".equals(currentUser.getRole()) && product.getProductState() != 0) {
                throw new IllegalStateException("판매 중인 상품만 삭제할 수 있습니다.");
            }
            productRepository.delete(product);
        } else {
            throw new SecurityException("Not authorized to delete this product");
        }
    }

    //검색용 메소드
    @Override
    public Page<ProductDto> searchProducts(String keyword, String categoryName, Pageable pageable) {
        // 1. Repository에서 페이징이 적용된 엔티티 Page 조회 (pageable 전달 필수)
        Page<Products> productsPage = productRepository.searchByFilters(keyword, categoryName, pageable);

        // 2. Page 객체의 map 기능을 사용하여 Entity를 DTO로 변환
        return productsPage.map(product -> {
            ProductDto dto = new ProductDto();
            dto.setProductId(product.getProductId());
            dto.setTitle(product.getTitle());
            dto.setPrice(product.getPrice());
            dto.setProductState(product.getProductState());
            dto.setCreatedAt(product.getCreatedAt());

            // 이미지 처리
            List<String> images = getImageUrls(product.getProductImage());
            if (images != null && !images.isEmpty()) {
                dto.setProductImage(images.get(0));
            }

            // 카테고리 처리
            if (product.getCategory() != null) {
                dto.setCategoryId(product.getCategory().getCategoryId());
            }

            return dto;
        });
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