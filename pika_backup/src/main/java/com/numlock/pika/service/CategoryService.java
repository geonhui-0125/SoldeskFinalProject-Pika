package com.numlock.pika.service;

import com.numlock.pika.domain.Categories;
import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<Categories> getAllCategories();
    Map<String, List<String>> getAllCategoriestoMap();
}
