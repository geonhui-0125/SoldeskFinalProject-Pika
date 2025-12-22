package com.numlock.pika.service;

import com.numlock.pika.domain.Categories;
import com.numlock.pika.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllCategoriestoMap() {
        List<Categories> categoriesList = categoryRepository.findAll();

        List<String> cateDeptwo = new ArrayList<>();

        Map<String, List<String>> cateDep =  new LinkedHashMap<>();

        String temp = "";

        for(Categories category : categoriesList) {
            String one = category.getCategory().split(">")[0];
            String two = category.getCategory().split(">")[1];

            if(!temp.equals(one)) {
                if(!temp.equals("")){
                    //저장
                    cateDep.put(temp,cateDeptwo);
                }
                temp = one;
                cateDeptwo = new ArrayList<>();
                cateDeptwo.add(two);
            }else {
                cateDeptwo.add(two);
            }
        }
        cateDep.put(temp,cateDeptwo);

        System.out.println(cateDep.get("피규어"));

        return cateDep;

    }
}
