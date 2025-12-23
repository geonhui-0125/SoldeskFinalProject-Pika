package com.numlock.pika.repository;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Accounts, Integer> {

    // @Query 활용 + JPQL 활용
    @Query(value = "SELECT a FROM Accounts a WHERE a.seller.id = :sellerId")
    Optional<Accounts> findByUserId(@Param("sellerId") String sellerId);

    Optional<Accounts> findBySeller(Users seller);

    boolean existsAccountsBySeller(Users seller);
}
