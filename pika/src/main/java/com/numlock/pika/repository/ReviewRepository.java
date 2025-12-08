package com.numlock.pika.repository;

import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Reviews, Integer> {

}
