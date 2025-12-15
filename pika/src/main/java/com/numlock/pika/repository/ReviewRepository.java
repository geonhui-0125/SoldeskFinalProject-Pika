package com.numlock.pika.repository;

import com.numlock.pika.domain.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    List<Reviews> findBySeller_Id(String sellerId);
    List<Reviews> findByReviewer_Id(String reviewerId);
}
