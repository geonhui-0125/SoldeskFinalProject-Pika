package com.numlock.pika.repository;

import java.util.List;

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
}
