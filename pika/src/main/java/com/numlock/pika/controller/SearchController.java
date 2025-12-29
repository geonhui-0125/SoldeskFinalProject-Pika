package com.numlock.pika.controller;

import com.numlock.pika.domain.Search;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.service.product.ProductService;
import com.numlock.pika.service.product.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final SearchService searchService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    // 검색용 메소드 수정
    @GetMapping
    public String searchProducts (
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String categoryName,
            Model model, Principal principal,
            @PageableDefault(size = 20) Pageable pageable){ // 기본 사이즈 15개 설정

        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);

        searchService.processSearch(keyword);

        // 2. 서비스 호출 (반환 타입을 Page로 변경)
        Page<ProductDto> productPage = productService.searchProducts(keyword, categoryName, pageable);

        // 3. 페이징 블록 계산 (메인과 동일한 로직)
        int blockLimit = 10;
        int totalPages = productPage.getTotalPages();
        int currentPage = productPage.getNumber();

        int startPage = (((int)(Math.ceil((double)(currentPage + 1) / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), totalPages);
        if (totalPages == 0) endPage = startPage;

        // 4. 모델에 데이터 추가
        model.addAttribute("products", productPage.getContent()); // getContent()로 리스트 추출
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", pageable.getPageSize());

        model.addAttribute("keyword", keyword);
        model.addAttribute("activeCategory", categoryName);

        if (principal != null) {

            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }

        return "product/search";
    }

    @GetMapping("/popular")
    @ResponseBody
    public List<Search> getPopularKeywordsApi(){
        return searchService.getPopularKeywords();
    }
}