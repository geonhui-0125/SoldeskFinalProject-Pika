package com.numlock.pika.repository;

import java.util.List;
import java.util.Optional;

import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.numlock.pika.domain.FavoriteProducts;
import com.numlock.pika.domain.Users;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProducts, Integer> {
	@Query(value = "select fp from FavoriteProducts fp join fetch fp.product where fp.user=:user")
	List<FavoriteProducts> findByUser(@Param("user") Users user);

	List<FavoriteProducts> findByProduct(@Param("product") Products product);

    int countByProduct(Products products);
    
    /*user와  product 엔티티로 데이터가 존재하는지 확인*/
    boolean existsByUserAndProduct(Users user, Products product);
    
    /*user와  product 엔티티로 존재하는 데이터 조회*/
    Optional<FavoriteProducts> findByUserAndProduct(Users user, Products product);
}
