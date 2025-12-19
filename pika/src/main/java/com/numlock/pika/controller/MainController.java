package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Principal principal,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getProductList(pageable);

        //페이징 블록 계산 로직 ---
        int blockLimit = 10;
        int totalPages = productPage.getTotalPages();
        int currentPage = productPage.getNumber();

        // 시작 페이지 계산 (1, 11, 21...)
        int startPage = (((int)(Math.ceil((double)(currentPage + 1) / blockLimit))) - 1) * blockLimit + 1;

        // 끝 페이지 계산 (10, 20, 30...)
        int endPage = Math.min((startPage + blockLimit - 1), totalPages);

        // 페이지가 아예 없을 때 에러 방지
        if (totalPages == 0) endPage = startPage;

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // 1. 카테고리 맵 추가 (헤더용)
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        // 2. 로그인 사용자 정보 추가 (헤더용)
        if (principal != null) {
            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", productPage.getSize());

        return "main";
    }
}
