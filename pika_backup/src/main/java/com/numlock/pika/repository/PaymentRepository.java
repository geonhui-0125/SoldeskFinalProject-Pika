package com.numlock.pika.repository;

import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, String> {

    Optional<Payments> findByBuyerId (String buyerId);

    Optional<Payments> findByBuyerIdAndTaskId(String buyerId, int taskId);
}
