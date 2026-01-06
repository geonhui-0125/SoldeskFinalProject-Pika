package com.numlock.pika.repository;

import com.numlock.pika.domain.Products;
import org.springframework.data.domain.Page; // 추가 필요
import org.springframework.data.domain.Pageable; // 추가 필요
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Integer>, JpaSpecificationExecutor {

    List<Products> findBySeller_Id(String sellerId);

    /**
     * 키워드와 카테고리를 이용한 동적 검색 쿼리 (페이징 지원)
     */
    @Query("SELECT p FROM Products p WHERE " +
            "(:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.description LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:categoryName IS NULL OR p.category.category LIKE CONCAT(:categoryName, '%'))")
    Page<Products> searchByFilters(@Param("keyword") String keyword,
                                   @Param("categoryName") String categoryName,
                                   Pageable pageable);

    @Query("SELECT AVG(p.price) FROM Products p WHERE p.title LIKE CONCAT('%', :keyword, '%') AND p.category.categoryId = :categoryId")
    Double findAveragePriceByTitleAndCategory(@Param("keyword") String keyword, @Param("categoryId") int categoryId);
}