package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.FavoriteProductService;
import com.numlock.pika.service.Notification.NotificationService;
import com.numlock.pika.service.product.ProductService;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.domain.Reviews;
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
	private final FavoriteProductService favoriteProductService;

    /**
     * [추가] 상품 검색 및 카테고리 필터링
     * 경로: GET /products/search
     */
    @GetMapping("/search")
    public String searchProducts(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "category", required = false) String category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 Model model, Principal principal) {

        // 1. 페이징 설정 (한 페이지 15개)
        Pageable pageable = PageRequest.of(page, size);

        // 2. 검색 로직 수행 (Service 메서드가 Page<ProductDto>를 반환하도록 수정되어야 함)
        Page<ProductDto> productPage = productService.searchProducts(keyword, category, pageable);

        // 3. 페이징 블록 계산 (10개 단위)
        int blockLimit = 10;
        int totalPages = productPage.getTotalPages();
        int currentPage = productPage.getNumber();

        int startPage = (((int) (Math.ceil((double) (currentPage + 1) / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), totalPages);

        if (totalPages == 0) endPage = startPage;

        // 4. 모델 데이터 추가
        model.addAttribute("products", productPage.getContent()); // 실제 상품 리스트
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        model.addAttribute("keyword", keyword);
        model.addAttribute("activeCategory", category);

        // 헤더 및 사용자 정보 (기존 로직 유지)
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        if (principal != null) {
            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }

        return "product/search";
    }


    /**
     * 관리자용 상품 목록 조회
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        // 1. 페이지 요청 생성 (한 페이지에 15개씩)
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getProductList(pageable);

        // 2. 페이징 블록 계산 로직 (10개 단위)
        int blockLimit = 10; // 하단에 보여줄 페이지 번호 개수
        int totalPages = productPage.getTotalPages();
        int currentPage = productPage.getNumber(); // 0부터 시작

        // 현재 페이지가 속한 블록의 시작 번호 (1, 11, 21...)
        int startPage = (((int) (Math.ceil((double) (currentPage + 1) / blockLimit))) - 1) * blockLimit + 1;

        // 현재 페이지가 속한 블록의 끝 번호 (10, 20, 30...)
        int endPage = Math.min((startPage + blockLimit - 1), totalPages);

        // 만약 totalPages가 0일 경우 endPage가 startPage보다 작아지는 것 방지
        if (totalPages == 0) endPage = startPage;

        // 3. 모델에 데이터 추가
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);

        model.addAttribute("startPage", startPage); // 시작 페이지 번호 추가
        model.addAttribute("endPage", endPage);     // 끝 페이지 번호 추가

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

        productService.upView(id);

        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        ProductDetailDto productDetailDto = productService.getProductDetailById(id, principal);

        System.out.println("productDetailDto = " + productDetailDto);

        model.addAttribute("productDetailDto", productDetailDto);
        model.addAttribute("review", new Reviews()); // 빈 Reviews 객체 추가

        if (principal != null) {
            //로그인한 사용자 아이디 호출
            String userId = principal.getName();

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

        if (principal != null) {
            //로그인한 사용자 아이디 호출
            String userId = principal.getName();

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

    /**
     * 현재 로그인한 유저의 찜 목록을 반환하는 API
     */
    @GetMapping("/api/favorites")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public List<ProductDto> getFavoriteProducts(Principal principal) {
        return userRepository.findById(principal.getName())
                .map(favoriteProductService::findFavoriteByUser)
                .orElse(new ArrayList<>());
    }
}