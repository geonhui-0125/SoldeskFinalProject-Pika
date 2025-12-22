package com.numlock.pika.repository;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {

    Optional<Categories> findByCategory(String categoryName);

}
