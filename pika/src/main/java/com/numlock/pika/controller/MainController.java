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
                       @RequestParam(defaultValue = "10") int size,
                       Principal principal,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> productPage = productService.getProductList(pageable);

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
