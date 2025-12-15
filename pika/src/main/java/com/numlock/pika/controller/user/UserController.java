package com.numlock.pika.controller.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.FavoriteProductRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.FavoriteProductService;
import com.numlock.pika.service.ProductService;
import com.numlock.pika.service.login.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

	private final UserRepository userRepository;
	private final FavoriteProductService favoriteProductService;

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

		return "myProducts";
	}
}
