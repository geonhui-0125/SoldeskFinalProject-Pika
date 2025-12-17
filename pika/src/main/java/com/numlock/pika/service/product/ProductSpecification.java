package com.numlock.pika.service.product;

import com.numlock.pika.domain.Products;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Products> search(String keyword, String category) {
      return null;
    }
}
