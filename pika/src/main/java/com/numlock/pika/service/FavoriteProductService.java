package com.numlock.pika.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.numlock.pika.domain.FavoriteProducts;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ProductDto;
import com.numlock.pika.repository.FavoriteProductRepository;
import com.numlock.pika.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteProductService{
	private final FavoriteProductRepository favoriteProductRepository;
	public List<ProductDto> findFavoriteByUser(Users user){
		/*
		 * favoriteProductRepository에서 user를 매개로 상품조회
		 * 컬렉션(list,set의 부모 클래스)의 메서드 stream()호출 : List를 새로운 Stream으로 변환해 생성
		 * map : stream내의 요소들을 부른다 요소는 favorite로 정의하고, 요소들의 getProduct()메서드를 호출해 그 리턴값으로 저장한다.
		 * Stream을 콜렉터즈의 toList메서드를 통해 List<Products>로 반환한다.
		 * 여기서 Collect는 interface고 Collectors는 다른 클래스다.
		 */
		List<FavoriteProducts> favoriteProducts = favoriteProductRepository.findByUser(user);
		return favoriteProducts.stream().map(favorite->favorite.getProduct())
				.map(products->ProductDto.fromEntity(products)).collect(Collectors.toList());
	}
	
}
