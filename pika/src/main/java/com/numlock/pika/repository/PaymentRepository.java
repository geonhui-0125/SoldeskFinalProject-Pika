package com.numlock.pika.repository;

import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, String> {

    List<Payments> findBySellerOrderByCreatedAtDesc(Users seller);

    List<Payments> findByBuyerOrderByCreatedAtDesc(Users buyer);

    Optional<Payments> findByBuyerAndTask(Users buyer, Products task);

    Optional<Payments> findByTask(Products task);
}
