package com.numlock.pika.repository;

import com.numlock.pika.domain.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<Users,String> {

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    //Optional<Users> findByEmail(String email);
    Optional<Users> findByIdAndEmail(String id, String email);
}
