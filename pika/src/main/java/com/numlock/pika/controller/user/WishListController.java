package com.numlock.pika.controller.user;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.service.ReviewService;
import com.numlock.pika.service.product.ProductService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.numlock.pika.service.user.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.FavoriteProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("user")
public class WishListController {

	private final UserRepository userRepository;
	private final FavoriteProductService favoriteProductService;
	private final ProductService productService;
	private final ReviewService reviewService;
	private final CategoryService categoryService;
	private final AccountService accountService;

	@GetMapping("wishlist")
	public String wishlist(Principal principal, Model model) {
		// 비로그인 유저 검사
		if (principal == null) {
			return "redirect:/";
		}
		String userId = principal.getName();// userid가져오기
		Optional<Users> optionalValue = userRepository.findById(userId);
		Users user = null;
		if (optionalValue.isPresent()) {
			user = optionalValue.get();
		}else {
			return "redirect:/";
		}
		model.addAttribute("userId",userId);
		List<ProductDto> wishlist = favoriteProductService.findFavoriteByUser(user);
		model.addAttribute("wishlist",wishlist);
		System.out.println("\n\n\n\n\n\n\n\n\n\n");
		System.out.println(wishlist);
		System.out.println("\n\n\n\n\n\n\n\n\n\n");
		return "user/wishlist";
	}

	@GetMapping("myProducts")
	public String myProducts(Principal principal, Model model) {
		if (principal == null) {
			return "redirect:/";
		}
		String userId = principal.getName();
		List<ProductDto> myProducts = productService.getProductsBySeller(userId);
		model.addAttribute("myProducts", myProducts);
		return "user/myProducts";
	}

	@GetMapping("mypage")
	public String myPage(Principal principal, Model model) {
		if (principal == null) {
			return "redirect:/user/login";
		}
		String userId = principal.getName();

		// Header
		Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
		model.addAttribute("categoriesMap", categoriesMap);

		// User Info
		Users user = userRepository.findById(userId).orElse(null);
		if(user == null) return "redirect:/";
		model.addAttribute("user", user);

		// 계좌 정보 조회/생성
		Accounts account = accountService.getOrCreateAccount(user);
		model.addAttribute("accounts", account);

		model.addAttribute("bankList", accountService.getBankList());

		// My Products
		List<ProductDto> myProducts = productService.getProductsBySeller(userId);
		model.addAttribute("myProducts", myProducts);

		// My Reviews (Store Reviews)
		List<ReviewResponseDto> myReviews = reviewService.getReviewsBySellerId(userId);
		model.addAttribute("myReviews", myReviews);

		// Reviews Written by Me
		List<ReviewResponseDto> writtenReviews = reviewService.getReviewsByWriterId(userId);
		model.addAttribute("writtenReviews", writtenReviews);

		// Wishlist
		List<ProductDto> wishlist = favoriteProductService.findFavoriteByUser(user);
		model.addAttribute("wishlist", wishlist);

		return "user/mypage";
	}

}
