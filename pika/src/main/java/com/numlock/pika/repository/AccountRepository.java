package com.numlock.pika.repository;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Accounts, Integer> {

    // @Query 활용 + JPQL 활용
    @Query(value = "SELECT a FROM Accounts a WHERE a.sellerId = : sellerId")
    Optional<Accounts> findByUserId(String sellerId);

}
