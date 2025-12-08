package com.numlock.pika.repository;

import com.numlock.pika.domain.Payments;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payments, String> {

}
