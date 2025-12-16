package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.dto.ProductRegisterDto;
import com.numlock.pika.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // ⭐️ 테스트를 위해 더미 데이터를 사용하는 임시 list 메서드 (DB 연결 불필요)
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        // ----------------------------------------------------
        // ⭐️ 1. 더미 데이터 100개 생성 (DB에 데이터가 없을 때 화면 테스트용)
        // ----------------------------------------------------
        List<ProductDto> allDummyProducts = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            ProductDto product = ProductDto.builder()
                    .productId(i)
                    .sellerId("seller" + (i % 5 + 1))
                    .categoryId(1)
                    .price(new BigDecimal(10000 + (i * 1000)))
                    .title("더미 상품 " + i + " - 테스트 데이터")
                    .description("이것은 더미 상품 " + i + "의 상세 설명입니다.")
                    .productImage("/upload/dummy_product_folder/" + (i % 10 + 1) + ".jpg")
                    .viewCnt(i * 10)
                    .createdAt(LocalDateTime.now().minusDays(100 - i))
                    .productState(0)
                    .build();
            allDummyProducts.add(product);
        }

        // ----------------------------------------------------
        // ⭐️ 2. 수동 페이징 처리 로직
        // ----------------------------------------------------
        int totalProducts = allDummyProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);

        int startIdx = page * size;
        int endIdx = Math.min(startIdx + size, totalProducts);

        List<ProductDto> paginatedProducts = new ArrayList<>();
        if (startIdx < totalProducts) {
            paginatedProducts = allDummyProducts.subList(startIdx, endIdx);
        }

        // 3. 모델에 데이터 및 페이징 정보 추가
        model.addAttribute("products", paginatedProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);

        return "main";
    }
    // --------------------------------------------------------------------------------------
    // ⚠️ 주의: 실제 DB 연동 시에는 아래 코드를 사용하도록 다시 수정해야 합니다. (현재는 주석 처리 필요)
    /*
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getProductList(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", productPage.getSize());

        return "main";
    }
    */
    // --------------------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") int id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/detail";
    }

    // ... (나머지 create, newProduct, registerProduct 메서드는 기존과 동일) ...

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
            model.addAttribute("product", productDto); // Keep input data
            // Assuming categoryService.getAllCategories() exists or is stubbed for testing
            // model.addAttribute("categories", categoryService.getAllCategories()); // Uncomment if needed
            return "product/form";
        }
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        Map<String, List<String>> cateMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("cateMap", cateMap);
        return "product/new";
    }

    @PostMapping("/register")
    public String registerProduct(ProductRegisterDto productRegisterDto,
                                  @RequestParam("images") List<MultipartFile> images, Principal principal) {

        System.out.println("dto 확인 : " + productRegisterDto);
        productService.registerProduct(productRegisterDto, principal, images);

        return "redirect:/products/new";
    }
}