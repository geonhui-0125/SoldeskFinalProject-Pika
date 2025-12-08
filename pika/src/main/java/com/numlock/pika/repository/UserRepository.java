package com.numlock.pika.repository;

import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, String> {

}
