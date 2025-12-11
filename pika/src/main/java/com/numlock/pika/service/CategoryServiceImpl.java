package com.numlock.pika.service;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categories> getAllCategories() {
        return categoryRepository.findAll();
    }
}
