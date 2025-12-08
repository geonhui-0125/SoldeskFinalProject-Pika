package com.numlock.pika.repository;

import com.numlock.pika.domain.FavoriteProducts;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProducts, Integer> {

}
