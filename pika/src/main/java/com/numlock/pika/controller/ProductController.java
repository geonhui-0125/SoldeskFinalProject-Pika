package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDetailDto;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.product.ProductService;
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
    private final com.numlock.pika.service.CategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
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
                //조회된 Users 객체를 "user"라는 이름으로 모델에 추가
                model.addAttribute("user", user);
            });

            //아이디만 전송하는 코드
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

    @PostMapping("/register")
    public String registerProduct(ProductRegisterDto productRegisterDto,
                                  @RequestParam("images") List<MultipartFile> images, Principal principal) {

        System.out.println("dto 확인 : " + productRegisterDto);
        productService.registerProduct(productRegisterDto, principal, images);

        return "redirect:/products/new";
    }


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

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") int id, ProductRegisterDto dto, Principal principal) {
        productService.updateProduct(id, dto, principal);
        return "redirect:/user/mypage";
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 접근 가능
    public String deleteProduct(@PathVariable("id") int id,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                Principal principal) {
        productService.deleteProduct(id, principal); // ProductServiceImpl은 판매자 또는 ADMIN 권한을 확인
        if ("productlist".equals(redirect)) {
            return "redirect:/products";
        }
        return "redirect:/user/mypage";
    }


    @GetMapping("/bySeller/{sellerId}")
    public String getProductsBySeller(@PathVariable("sellerId") String sellerId, Model model) {
        List<ProductDto> products = productService.getProductsBySeller(sellerId);
        model.addAttribute("products", products);
        return "product/list";
    }

    //검색용 메소드
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String categoryName,
            Model model) {

        // 1. 헤더 출력을 위한 카테고리 맵 다시 담기 (헤더가 공통이라 매번 필요함)
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        // 2. 서비스에서 필터링된 리스트 가져오기
        // categoryName이 "피규어"면 하위 항목 포함 검색, "피규어>인형"이면 정밀 검색 로직 필요
        List<ProductDto> productList = productService.searchProducts(keyword, categoryName);

        model.addAttribute("products", productList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeCategory", categoryName);

        // 3. 검색 결과를 보여줄 페이지 (main.html과 구조가 같다면 main을 재사용해도 됨)
        return "product/search";
    }

}

