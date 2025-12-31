package com.numlock.pika.service.user;

import com.numlock.pika.domain.FavoriteProducts;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.FpApiDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.FavoriteProductRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.Notification.NotificationService;
import com.numlock.pika.service.product.ProductService;
import com.numlock.pika.service.product.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class WishApiService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FavoriteProductRepository favoriteProductRepository;
    private final NotificationService notificationService;
    private final ProductServiceImpl productServiceImpl;

    public FpApiDto upWishCount(int productId, Principal principal) {

        Products products = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 상품 정보를 찾지 못했습니다."));

        Users users = userRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보를 찾지 못했습니다."));
        
        FavoriteProducts favoriteProducts = FavoriteProducts.builder()
                                .user(users)
                                .product(products)
                                .build();

        notificationService.sendSellerProductWished(productId, principal);

        favoriteProductRepository.save(favoriteProducts);

        return FpApiDto.builder()
                .productId(favoriteProducts.getProduct().getProductId())
                .productImage(productServiceImpl.getImageUrls(favoriteProducts.getProduct().getProductImage()).get(0))
                .fpCnt(favoriteProductRepository.countByProduct(products))
                .build();
    }

    public FpApiDto downWishCount(int productId, Principal principal) {

        Products products = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품 정보를 찾지 못했습니다."));

        Users users = userRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보를 찾지 못했습니다."));

        boolean wished = favoriteProductRepository.existsByUserAndProduct(users, products);

        FavoriteProducts favoriteProducts = FavoriteProducts.builder()
                .user(users)
                .product(products)
                .build();

        System.out.println("찜을 했는지 확인 : " + wished );

        if(wished) {
            favoriteProductRepository.findByUserAndProduct(users, products)
                    .ifPresent(favoriteProductRepository::delete);
        }

        return FpApiDto.builder()
                .productId(favoriteProducts.getProduct().getProductId())
                .build();
    }

}
