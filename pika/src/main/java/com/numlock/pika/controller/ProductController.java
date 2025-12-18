package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.Notification.NotificationService;
import com.numlock.pika.service.product.ProductService;
import com.numlock.pika.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // ArrayList 임포트 추가

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * [추가] 상품 검색 및 카테고리 필터링
     * 경로: GET /products/search
     */
    @GetMapping("/search")
    public String searchProducts(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "category", required = false) String category,
                                 Model model, Principal principal) {

        // 헤더 카테고리 메뉴 구성을 위한 데이터
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        // 검색 로직 수행 (Service에서 keyword와 category를 처리)
        List<ProductDto> products = productService.searchProducts(keyword, category);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeCategory", category);

        // 헤더 사용자 프로필 처리를 위한 로직
        if (principal != null) {
            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }

        return "product/search"; // search.html 렌더링
    }

    /**
     * 관리자용 상품 목록 조회
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getProductList(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", productPage.getSize());

        return "product/list";
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String detail(@PathVariable("id") int id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/detail";
    }


    // ... (나머지 create, newProduct, registerProduct 메서드는 기존과 동일) ...

    @GetMapping("/info/{id}")
    public String detail2(@PathVariable("id") int id, Principal principal, Model model) {

        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        ProductDetailDto productDetailDto = productService.getProductDetailById(id, principal);

        System.out.println("productDetailDto = " + productDetailDto);

        model.addAttribute("productDetailDto", productDetailDto);

        if(principal != null) {
            //로그인한 사용자 아이디 호출
            String userId =  principal.getName();

            System.out.println("login한 사용자 : " + userId);

            //아이디를 이용해 DB에서 사용자 조회
            userRepository.findById(userId).ifPresent(user -> {
                model.addAttribute("user", user);
            });
            model.addAttribute("loginUserId", userId);
        }

        return "product/info";
    }

    @PostMapping
    public String create(@ModelAttribute ProductDto productDto, java.security.Principal principal, Model model) {
        try {
            if (principal != null) {
                productDto.setSellerId(principal.getName());
            } else if (productDto.getSellerId() == null || productDto.getSellerId().isEmpty()) {
                productDto.setSellerId("user1");
            }
            //productService.registerProduct(productDto, principal);
            return "redirect:/products";
        } catch (Exception e) {
            e.printStackTrace(); // Log error to console
            model.addAttribute("errorMessage", "Error registering product: " + e.getMessage());
            model.addAttribute("product", productDto);
            return "product/form";
        }
    }

    @GetMapping("/new")
    public String newProduct(Model model, Principal principal) {
        if (principal == null) return "redirect:/user/login";

        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();

        model.addAttribute("categoriesMap", categoriesMap);

        if(principal != null) {
            //로그인한 사용자 아이디 호출
            String userId =  principal.getName();

            System.out.println("login한 사용자 : " + userId);

            //아이디를 이용해 DB에서 사용자 조회
            userRepository.findById(userId).ifPresent(user -> {
                //조회된 Users 객체를 "user"라는 이름으로 모델에 추가
                model.addAttribute("user", user);
            });

            //아이디만 전송하는 코드
            model.addAttribute("loginUserId", userId);
        }


        return "product/new";
    }

    /**
     * 실제 상품 등록 처리
     */
    @PostMapping("/register")
    public String registerProduct(ProductRegisterDto productRegisterDto,
                                  @RequestParam("images") List<MultipartFile> images, Principal principal) {

        System.out.println("dto 확인 : " + productRegisterDto);
        productService.registerProduct(productRegisterDto, principal, images);
        return "redirect:/"; // 등록 후 메인으로 이동
    }

    /**
     * 상품 수정 페이지 이동
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") int id, Principal principal, Model model) {
        ProductDetailDto product = productService.getProductDetailById(id, principal);

        if (!product.getSellerId().equals(principal.getName())) {
            return "redirect:/products/info/" + id;
        }

        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);
        model.addAttribute("product", product);

        // Transform "Main>Sub" to "Main > Sub" for the form if needed
        String category = product.getCategory();
        if (category != null && category.contains(">")) {
            model.addAttribute("currentCategory", category.replace(">", " > "));
        }

        return "product/edit";
    }

    /**
     * 상품 정보 업데이트 처리
     */
    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") int id, ProductRegisterDto dto, Principal principal) {
        notificationService.sendProductChange(id, dto);
        productService.updateProduct(id, dto, principal);
        return "redirect:/user/mypage";
    }

    /**
     * 상품 삭제 처리
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public String deleteProduct(@PathVariable("id") int id,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                Principal principal) {
        productService.deleteProduct(id, principal);
        if ("productlist".equals(redirect)) {
            return "redirect:/products";
        }
        return "redirect:/user/mypage";
    }

    /**
     * 특정 판매자의 상품 목록 조회
     */
    @GetMapping("/bySeller/{sellerId}")
    public String getProductsBySeller(@PathVariable("sellerId") String sellerId, Model model) {
        List<ProductDto> products = productService.getProductsBySeller(sellerId);
        model.addAttribute("products", products);
        return "product/list";
    }
}