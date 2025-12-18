package com.numlock.pika.service.product;

import com.numlock.pika.domain.Search;

import java.util.List;

public interface SearchService {
    void processSearch(String keyword);

    List<Search> getPopularKeywords();
}
