package com.numlock.pika.repository;

import com.numlock.pika.domain.Reviews;
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

}
