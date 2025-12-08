package com.numlock.pika.repository;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {

}
