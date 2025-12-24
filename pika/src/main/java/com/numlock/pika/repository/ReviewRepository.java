package com.numlock.pika.repository;

import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Reviews;
import com.numlock.pika.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    @Query("SELECT r FROM Reviews r JOIN FETCH r.reviewer WHERE r.seller.id = :sellerId")
    List<Reviews> findBySeller_Id(String sellerId);
    List<Reviews> findByReviewer_Id(String reviewerId);

    @Query("SELECT r FROM Reviews r JOIN FETCH r.reviewer WHERE r.reviewId = :reviewId")
    Optional<Reviews> findById(Long reviewId);

    // 상품 하나에 대해 한 명의 작성자가 한 개의 리뷰만 작성할 수 있도록 확인하는 메서드
    boolean existsByProductAndReviewer(Products product, Users reviewer);

    // 특정 사용자가 특정 상품에 대해 리뷰를 작성했는지 확인하는 메서드 (ID 기반)
    boolean existsByReviewer_IdAndProduct_ProductId(String reviewerId, int productId);
}
