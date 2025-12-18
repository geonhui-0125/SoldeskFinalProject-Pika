package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "search")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_seq_generator")
    @SequenceGenerator(name = "search_seq_generator", sequenceName = "seq_search", allocationSize = 1)
    @Column(name = "search_id")
    private Long searchId;

    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;

    @Column(name = "search_count")
    private Long searchCount;

    public void incrementSearchCount() {
        this.searchCount = this.searchCount == null ? 1 : this.searchCount + 1;
    }

}
