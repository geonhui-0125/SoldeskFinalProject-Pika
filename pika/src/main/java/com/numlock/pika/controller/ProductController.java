package com.numlock.pika.controller;

import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final com.numlock.pika.service.CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        List<ProductDto> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") int id, Model model) {
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        List<com.numlock.pika.domain.Categories> categories = categoryService.getAllCategories();
        System.out.println("DEBUG: Fetched categories size: " + categories.size());
        model.addAttribute("product", new ProductDto());
        model.addAttribute("categories", categories);
        return "product/form";
    }

    @PostMapping
    public String create(@ModelAttribute ProductDto productDto, java.security.Principal principal, Model model) {
        try {
            if (principal != null) {
                productDto.setSellerId(principal.getName());
            } else if (productDto.getSellerId() == null || productDto.getSellerId().isEmpty()) {
                // If not logged in and no sellerId provided, we can't create product
                // But normally Security config protects this.
                // Fallback for dev/test without login
                productDto.setSellerId("user1"); 
            }
            
            productService.createProduct(productDto);
            return "redirect:/products";
        } catch (Exception e) {
            e.printStackTrace(); // Log error to console
            model.addAttribute("errorMessage", "Error registering product: " + e.getMessage());
            model.addAttribute("product", productDto); // Keep input data
            model.addAttribute("categories", categoryService.getAllCategories()); // Reload categories
            return "product/form";
        }
    }
}
