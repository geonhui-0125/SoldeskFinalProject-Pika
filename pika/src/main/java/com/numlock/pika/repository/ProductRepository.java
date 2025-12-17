package com.numlock.pika.repository;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Integer>, JpaSpecificationExecutor {

    /**
     * 상품정보와 판매자 정보를 조인
     * 상품 정보 페이지에서 판매자 정보도 가져올 때 사용
     * @param int productId
     * @return Optional<Products>
     */
    @Query(value = "SELECT p FROM Products p LEFT JOIN FETCH p.seller s WHERE p.productId = :productId")
    Optional<Products> findWithUserById(int productId);

    List<Products> findBySeller_Id(String sellerId);
}
