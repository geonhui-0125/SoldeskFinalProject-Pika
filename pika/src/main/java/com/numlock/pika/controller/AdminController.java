package com.numlock.pika.controller;

import com.numlock.pika.dto.user.AdminUserDto;
import com.numlock.pika.dto.ProductDto; // ProductDto 임포트 추가
import com.numlock.pika.service.AdminProductService; // AdminProductService 임포트 추가
import com.numlock.pika.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // 모든 관리자 기능에 ADMIN 역할 필요
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminProductService adminProductService; // AdminProductService 주입

    // 관리자 사용자 목록 페이지를 표시합니다.
    @GetMapping("/users")
    public String adminUsersPage(Model model) {
        // Thymeleaf 템플릿에서 직접 사용자 목록을 로드할 수도 있고,
        // API 호출을 통해 비동기적으로 로드할 수도 있습니다.
        // 여기서는 비동기 로딩을 가정하고 페이지 템플릿만 제공합니다.
        return "admin/users"; // /templates/admin/users.html
    }

    // 관리자 제품 목록 페이지를 표시합니다.
    @GetMapping("/products")
    public String adminProductsPage(Model model) {
        return "admin/products"; // /templates/admin/products.html
    }

    // 모든 사용자 목록을 가져오는 REST API 엔드포인트
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        List<AdminUserDto> users = adminUserService.getAllUsersForAdmin();
        return ResponseEntity.ok(users);
    }

    // 모든 제품 목록을 가져오는 REST API 엔드포인트
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = adminProductService.getAllProductsForAdmin();
        return ResponseEntity.ok(products);
    }

    // 사용자에게 경고를 부여하는 REST API 엔드포인트
    @PostMapping("/api/users/{userId}/warn")
    @ResponseBody
    public ResponseEntity<String> warnUser(@PathVariable String userId) {
        try {
            adminUserService.warnUser(userId);
            return ResponseEntity.ok("User " + userId + " warned successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to warn user: " + e.getMessage());
        }
    }

    // 사용자를 제한하는 REST API 엔드포인트
    @PostMapping("/api/users/{userId}/restrict")
    @ResponseBody
    public ResponseEntity<String> restrictUser(@PathVariable String userId,
                                               @RequestBody Map<String, String> payload) {
        try {
            String reason = payload.get("reason");
            String endDateStr = payload.get("endDate");
            LocalDateTime endDate = null;
            if (endDateStr != null && !endDateStr.isEmpty()) {
                // ISO_LOCAL_DATE_TIME (yyyy-MM-ddTHH:mm:ss) 형식으로 파싱
                endDate = LocalDateTime.parse(endDateStr);
            }

            adminUserService.restrictUser(userId, reason, endDate);
            return ResponseEntity.ok("User " + userId + " restricted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to restrict user: " + e.getMessage());
        }
    }

    // 사용자의 제한을 해제하는 REST API 엔드포인트
    @PostMapping("/api/users/{userId}/unrestrict")
    @ResponseBody
    public ResponseEntity<String> unrestrictUser(@PathVariable String userId) {
        try {
            adminUserService.liftRestriction(userId);
            return ResponseEntity.ok("User " + userId + " unrestricted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to unrestrict user: " + e.getMessage());
        }
    }

    // 제품을 정책 위반으로 표시하는 REST API 엔드포인트
    @PostMapping("/api/products/{productId}/markViolation")
    @ResponseBody
    public ResponseEntity<String> markProductAsViolatingPolicy(@PathVariable int productId,
                                                               @RequestBody Map<String, String> payload) {
        try {
            String reason = payload.get("reason");
            adminProductService.markProductAsViolatingPolicy(productId, reason);
            return ResponseEntity.ok("Product " + productId + " marked as violating policy successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to mark product as violating policy: " + e.getMessage());
        }
    }

    // 제품을 삭제하는 REST API 엔드포인트
    @PostMapping("/api/products/{productId}/delete")
    @ResponseBody
    public ResponseEntity<String> deleteProductByAdmin(@PathVariable int productId,
                                                       @RequestBody Map<String, String> payload) {
        try {
            String reason = payload.get("reason");
            adminProductService.deleteProductByAdmin(productId, reason);
            return ResponseEntity.ok("Product " + productId + " deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete product: " + e.getMessage());
        }
    }

    // 제품 정책 위반 상태를 해제하는 REST API 엔드포인트
    @PostMapping("/api/products/{productId}/liftViolation")
    @ResponseBody
    public ResponseEntity<String> liftProductViolation(@PathVariable int productId) {
        try {
            adminProductService.liftProductViolation(productId);
            return ResponseEntity.ok("Product " + productId + " policy violation lifted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to lift product policy violation: " + e.getMessage());
        }
    }
}
