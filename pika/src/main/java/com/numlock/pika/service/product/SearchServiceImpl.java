package com.numlock.pika.service.product;

import com.numlock.pika.domain.Search;
import com.numlock.pika.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchServiceImpl implements SearchService {
    private final SearchRepository searchRepository;

    @Override
    public void processSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        Search search = searchRepository.findByKeyword(keyword.trim())
                .orElseGet(() -> Search.builder()
                        .keyword(keyword.trim())
                        .searchCount(0L)
                        .build());
        search.incrementSearchCount();
        searchRepository.save(search);
    }
}
