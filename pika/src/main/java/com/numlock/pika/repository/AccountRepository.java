package com.numlock.pika.repository;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Accounts, Integer> {

}
