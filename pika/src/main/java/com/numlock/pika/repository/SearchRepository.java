package com.numlock.pika.repository;

import com.numlock.pika.domain.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long> {
    Optional<Search> findByKeyword(String keyword);

    @Query("SELECT s FROM Search s order by s.searchCount DESC")
    List<Search> findTopPopular(Pageable pageable);
}
