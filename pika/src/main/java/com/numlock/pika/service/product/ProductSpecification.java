package com.numlock.pika.service.product;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.domain.Products;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Products> search(String keyword, String categoryName) {
      return (root, query, criteriaBuilder) -> {
          List<Predicate> predicates = new ArrayList<>();
          //1. 검색어(keyword)와 title 포함 검사
          if (keyword != null && !keyword.trim().isEmpty()) {
              predicates.add(criteriaBuilder.like(root.get("title"),"%" + keyword + "%"));
          }
          //2. categoryName이 있으면 카테고리 이름으로 필터링
          if (categoryName != null && !categoryName.trim().isEmpty()) {
              Join<Products, Categories> categoryJoin = root.join("category");
              predicates.add(criteriaBuilder.equal(categoryJoin.get("category"), categoryName));
          }
          //판매중인 상품만 조회
          // predicates.add(criteriaBuilder.equal(root.get("productState"),0));
          //최신순 정렬
          query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

          return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
      };
    }
}
