package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.service.product.ProductService;
import com.numlock.pika.service.product.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final SearchService searchService;

    //검색용 메소드
    @GetMapping
    public String searchProducts (
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String categoryName,
            Model model){

        searchService.processSearch(keyword);
        List<ProductDto> productList = productService.searchProducts(keyword, categoryName);
        model.addAttribute("products", productList);
        model.addAttribute("keyword", keyword); //검색어 화면에 표시
        model.addAttribute("activeCategory", categoryName); //현재 카테고리 표시

        return "product/search";
    }
}
